package com.gustasousagh.studify.estudify.services.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.gustasousagh.studify.estudify.entity.SubscriptionEntity;
import com.gustasousagh.studify.estudify.integrations.client.StripeClient;
import com.gustasousagh.studify.estudify.model.response.SubscriptionPlanResponse;
import com.gustasousagh.studify.estudify.repository.SubscriptionRepository;
import com.gustasousagh.studify.estudify.services.UserPaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.Invoice;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class UserPaymentServiceImpl implements UserPaymentService {

    @Value("${stripe.webhook-secret}")
    private String endpointSecret;

    private final SubscriptionRepository subscriptionRepository;
    private final StripeClient stripeClient;

    @Override
    public String createCheckoutSession() {
        // TODO: trocar "1L" pelo ID real do usuário autenticado
        final String userId = "1L";

        SubscriptionEntity existing = subscriptionRepository.findByUserId(userId);
        String stripeCustomerId;

        if (existing == null || existing.getStripeCustomerId() == null) {
            final var stripeCustomer = stripeClient.createCustomer("gustavo.sousa@ilegra.com", "Gustavo Sousa");
            stripeCustomerId = stripeCustomer.getId();

            SubscriptionEntity sub = new SubscriptionEntity();
            sub.setUserId(userId);
            sub.setStripeCustomerId(stripeCustomerId);
            sub.setStatus("pending");
            subscriptionRepository.save(sub);
        } else {
            stripeCustomerId = existing.getStripeCustomerId();
        }

        // se já tiver assinatura ativa, não cria novo checkout (evita pagar 2x)
        if (existing != null
                && existing.getStripeSubscriptionId() != null
                && ("active".equalsIgnoreCase(existing.getStatus()) || "trialing".equalsIgnoreCase(existing.getStatus()))) {
            // Você pode retornar uma URL da área logada ou lançar uma exceção de regra de negócio
            // Aqui só retornamos null/uma string indicando já ativo (ajuste conforme seu front)
            return "ALREADY_ACTIVE";
        }

        return stripeClient.createCheckoutSession(
                stripeCustomerId,
                "price_1RxfNBE3ZKBHarUm1TxMJIMi",
                "http://localhost:3000/subscription/success",
                "http://localhost:3000/subscription/cancel"
        );
    }

    @Override
    public void handlePaymentSuccessWebhook(String payload, String signatureHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, signatureHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            System.out.println("!! Webhook assinatura inválida");
            return; // responde 200 no controller, mas não processa
        }

        final String type = event.getType();
        final EventDataObjectDeserializer des = event.getDataObjectDeserializer();

        if (des.getObject().isEmpty()) {
            System.out.println("?? " + type + " sem objeto deserializado; usando RAW + retrieve()");
        }

        try {
            switch (type) {
                case "checkout.session.completed": {
                    String sessionId = getIdFromRaw(des);
                    if (sessionId == null) {
                        System.out.println("!! checkout.session.completed sem ID");
                        break;
                    }

                    Session fullSession = Session.retrieve(sessionId);
                    String customerId = fullSession.getCustomer();
                    String subscriptionId = fullSession.getSubscription();

                    if (customerId == null) {
                        System.out.println("!! checkout.session.completed sem customerId");
                        break;
                    }

                    SubscriptionEntity sub = ensureByCustomer(customerId);
                    sub.setStripeSubscriptionId(subscriptionId);
                    sub.setStatus("active");
                    subscriptionRepository.save(sub);

                    System.out.println("✅ checkout.session.completed processado");
                    break;
                }

                case "customer.subscription.created": {
                    String subId = getIdFromRaw(des);
                    if (subId == null) {
                        System.out.println("!! subscription.created sem ID");
                        break;
                    }
                    Subscription stripeSub = Subscription.retrieve(subId);

                    SubscriptionEntity sub = findBySubscriptionOrCustomer(
                            stripeSub.getId(), stripeSub.getCustomer());
                    if (sub == null) {
                        sub = new SubscriptionEntity();
                        sub.setUserId("1L"); // TODO: vincular corretamente ao usuário
                        sub.setStripeCustomerId(stripeSub.getCustomer());
                    }
                    sub.setStripeSubscriptionId(stripeSub.getId());
                    sub.setStatus(stripeSub.getStatus());
                    fillPeriods(stripeSub, sub);
                    subscriptionRepository.save(sub);

                    System.out.println("✅ customer.subscription.created processado");
                    break;
                }

                case "customer.subscription.updated": {
                    String subId = getIdFromRaw(des);
                    if (subId == null) {
                        System.out.println("!! subscription.updated sem ID");
                        break;
                    }
                    Subscription stripeSub = Subscription.retrieve(subId);

                    SubscriptionEntity sub = subscriptionRepository.findByStripeSubscriptionId(stripeSub.getId());
                    if (sub == null) {
                        sub = subscriptionRepository.findByStripeCustomerId(stripeSub.getCustomer());
                    }
                    if (sub != null) {
                        sub.setStatus(stripeSub.getStatus());
                        fillPeriods(stripeSub, sub);
                        subscriptionRepository.save(sub);
                        System.out.println("✅ customer.subscription.updated processado");
                    } else {
                        System.out.println("!! subscription.updated sem registro correspondente");
                    }
                    break;
                }

                case "customer.subscription.deleted": {
                    String subId = getIdFromRaw(des);
                    if (subId == null) {
                        System.out.println("!! subscription.deleted sem ID");
                        break;
                    }
                    Subscription stripeSub = Subscription.retrieve(subId);

                    SubscriptionEntity sub = subscriptionRepository.findByStripeSubscriptionId(stripeSub.getId());
                    if (sub == null) {
                        sub = subscriptionRepository.findByStripeCustomerId(stripeSub.getCustomer());
                    }
                    if (sub != null) {
                        sub.setStatus("canceled");
                        if (stripeSub.getCanceledAt() != null) {
                            sub.setCanceledAt(
                                    Instant.ofEpochSecond(stripeSub.getCanceledAt())
                                            .atZone(ZoneId.systemDefault())
                                            .toLocalDateTime()
                            );
                        }
                        subscriptionRepository.save(sub);
                        System.out.println("✅ customer.subscription.deleted processado");
                    } else {
                        System.out.println("!! subscription.deleted sem registro correspondente");
                    }
                    break;
                }

                case "invoice.paid": {
                    String invoiceId = getIdFromRaw(des);
                    if (invoiceId == null) {
                        System.out.println("!! invoice.paid sem ID");
                        break;
                    }
                    Invoice invoice = Invoice.retrieve(invoiceId);

                    if (invoice.getSubscription() != null) {
                        SubscriptionEntity sub =
                                subscriptionRepository.findByStripeSubscriptionId(invoice.getSubscription());
                        if (sub != null) {
                            sub.setStatus("active"); // renovou com sucesso
                            subscriptionRepository.save(sub);
                            System.out.println("✅ invoice.paid processado");
                        }
                    }
                    break;
                }

                case "invoice.payment_failed": {
                    String invoiceId = getIdFromRaw(des);
                    if (invoiceId == null) {
                        System.out.println("!! invoice.payment_failed sem ID");
                        break;
                    }
                    Invoice invoice = Invoice.retrieve(invoiceId);

                    if (invoice.getSubscription() != null) {
                        SubscriptionEntity sub =
                                subscriptionRepository.findByStripeSubscriptionId(invoice.getSubscription());
                        if (sub != null) {
                            sub.setStatus("past_due"); // falhou o pagamento
                            subscriptionRepository.save(sub);
                            System.out.println("✅ invoice.payment_failed processado");
                        }
                    }
                    break;
                }

                default:
                    System.out.println("?? Unhandled event type: " + type);
            }
        } catch (Exception e) {
            // loga e deixa o controller devolver 200
            e.printStackTrace();
        }
    }

    @Override
    public SubscriptionPlanResponse getSubscriptionPlan() {
        SubscriptionEntity subscription = subscriptionRepository.findByUserId("1L"); // TODO: vincular corretamente ao usuário autenticado
        SubscriptionPlanResponse response = new SubscriptionPlanResponse();
        if (subscription != null) {
            response.setPlan(subscription.getStripeSubscriptionId() != null ? "active" : "inactive");
        } else {
            response.setPlan("inactive");
        }
        return response;
    }

    // ----------------- Helpers -----------------

    /** Extrai o "id" do objeto cru do evento (quando o deserializer não popula o model). */
    /** Extrai o "id" do JSON cru do evento (quando o deserializer não popula o model). */
    private String getIdFromRaw(EventDataObjectDeserializer des) {
        String raw = des.getRawJson(); // <- é String nessa versão do stripe-java
        if (raw == null || raw.isBlank()) return null;

        try {
            JsonObject obj = JsonParser.parseString(raw).getAsJsonObject();
            if (obj.has("id") && !obj.get("id").isJsonNull()) {
                return obj.get("id").getAsString();
            }
        } catch (Exception e) {
            System.out.println("!! Falha ao parsear raw JSON: " + e.getMessage());
        }
        return null;
    }


    /** Atualiza períodos a partir do objeto Subscription da Stripe. */
    private void fillPeriods(Subscription stripeSub, SubscriptionEntity entity) {
        if (stripeSub.getCurrentPeriodStart() != null) {
            entity.setCurrentPeriodStart(
                    Instant.ofEpochSecond(stripeSub.getCurrentPeriodStart())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime()
            );
        }
        if (stripeSub.getCurrentPeriodEnd() != null) {
            entity.setCurrentPeriodEnd(
                    Instant.ofEpochSecond(stripeSub.getCurrentPeriodEnd())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime()
            );
        }
    }

    /** Garante que exista uma SubscriptionEntity para o customer dado. */
    private SubscriptionEntity ensureByCustomer(String customerId) {
        SubscriptionEntity sub = subscriptionRepository.findByStripeCustomerId(customerId);
        if (sub == null) {
            sub = new SubscriptionEntity();
            sub.setUserId("1L"); // TODO: vincular ao usuário autenticado
            sub.setStripeCustomerId(customerId);
            sub.setStatus("pending");
        }
        return sub;
    }

    /** Tenta buscar por subscriptionId, senão por customerId. */
    private SubscriptionEntity findBySubscriptionOrCustomer(String subscriptionId, String customerId) {
        SubscriptionEntity sub = null;
        if (subscriptionId != null) {
            sub = subscriptionRepository.findByStripeSubscriptionId(subscriptionId);
        }
        if (sub == null && customerId != null) {
            sub = subscriptionRepository.findByStripeCustomerId(customerId);
        }
        return sub;
    }
}

CREATE TABLE IF NOT EXISTS course_topic (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    course_id BIGINT UNSIGNED NOT NULL,
    topic_title VARCHAR(255) NOT NULL,
    topic_description TEXT,
    topic_content TEXT,
    order_number INT NOT NULL,
    PRIMARY KEY (id),
     complete boolean NOT NULL DEFAULT false,
     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_course
      FOREIGN KEY (course_id)
      REFERENCES course(id)
      ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
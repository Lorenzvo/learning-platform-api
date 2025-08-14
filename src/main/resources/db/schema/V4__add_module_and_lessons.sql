-- Creates "modules" and "lessons" with FKs and useful indexes.

-- MODULES
CREATE TABLE IF NOT EXISTS modules (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,  -- PK
    course_id    BIGINT       NOT NULL,              -- FK -> courses.id
    title        VARCHAR(200) NOT NULL,              -- Module title
    position     INT          NOT NULL,              -- Order within course (0+)
    description  TEXT         NULL,                  -- Optional blurb

    CONSTRAINT fk_modules_course
    FOREIGN KEY (course_id) REFERENCES courses(id)
    ON DELETE CASCADE          -- delete modules if course is deleted
    ON UPDATE RESTRICT,

    CONSTRAINT ck_modules_position_nonneg CHECK (position >= 0)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Lookup per course in order
CREATE INDEX idx_modules_course_position ON modules(course_id, position);

-- LESSONS
CREATE TABLE IF NOT EXISTS lessons (
    id               BIGINT PRIMARY KEY AUTO_INCREMENT, -- PK
    module_id        BIGINT        NOT NULL,            -- FK -> modules.id
    title            VARCHAR(200)  NOT NULL,            -- Lesson title
    type             VARCHAR(16)   NOT NULL,            -- 'VIDEO' | 'TEXT' (EnumType.STRING)
    content_url      VARCHAR(512)  NULL,                -- Media/doc URL
    duration_seconds INT           NULL,                -- Nullable; must be >= 0 if set
    is_demo          TINYINT(1)    NOT NULL DEFAULT 0,  -- MySQL boolean

    CONSTRAINT fk_lessons_module
    FOREIGN KEY (module_id) REFERENCES modules(id)
    ON DELETE CASCADE -- delete lessons if module is deleted
    ON UPDATE RESTRICT,

    -- Keep DB aligned with enum values
    CONSTRAINT ck_lessons_type CHECK (type IN ('VIDEO','TEXT')),
    CONSTRAINT ck_lessons_duration_nonneg CHECK (duration_seconds IS NULL OR duration_seconds >= 0)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Fast per-module queries
CREATE INDEX idx_lessons_module ON lessons(module_id);
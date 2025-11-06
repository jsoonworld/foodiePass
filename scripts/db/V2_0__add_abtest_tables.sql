-- FoodiePass MVP v2 - Database Migration
-- Version: 2.0
-- Description: Add A/B Test and Survey tables for hypothesis validation
-- Author: Integration Team
-- Date: 2025-11-04

-- ============================================================================
-- Table: menu_scan
-- Purpose: Store menu scan sessions with A/B group assignment
-- Dependencies: None
-- ============================================================================

CREATE TABLE IF NOT EXISTS menu_scan (
    id BINARY(16) PRIMARY KEY COMMENT 'UUID as binary',
    user_id VARCHAR(255) NOT NULL COMMENT 'Session ID or user identifier',
    ab_group VARCHAR(20) NOT NULL COMMENT 'CONTROL or TREATMENT',
    image_url VARCHAR(500) COMMENT 'S3 or storage URL for menu image',
    source_language VARCHAR(50) NOT NULL COMMENT 'Original menu language',
    target_language VARCHAR(50) NOT NULL COMMENT 'User preferred language',
    source_currency VARCHAR(10) NOT NULL COMMENT 'Original menu currency',
    target_currency VARCHAR(10) NOT NULL COMMENT 'User preferred currency',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Scan creation time',

    -- Indexes for query optimization
    INDEX idx_user_id (user_id),
    INDEX idx_ab_group (ab_group),
    INDEX idx_created_at (created_at),

    -- Constraints
    CONSTRAINT chk_ab_group CHECK (ab_group IN ('CONTROL', 'TREATMENT')),
    CONSTRAINT chk_languages CHECK (
        source_language IS NOT NULL
        AND target_language IS NOT NULL
        AND source_language != target_language
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Menu scan sessions with A/B group assignment for hypothesis testing';

-- ============================================================================
-- Table: survey_response
-- Purpose: Store user confidence survey responses for A/B test validation
-- Dependencies: menu_scan (FK)
-- ============================================================================

CREATE TABLE IF NOT EXISTS survey_response (
    id BINARY(16) PRIMARY KEY COMMENT 'UUID as binary',
    scan_id BINARY(16) NOT NULL COMMENT 'FK to menu_scan',
    ab_group VARCHAR(20) NOT NULL COMMENT 'Denormalized from menu_scan for analytics',
    has_confidence BOOLEAN NOT NULL COMMENT 'User confidence: true=Yes, false=No',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Response submission time',

    -- Foreign Key
    CONSTRAINT fk_survey_scan_id FOREIGN KEY (scan_id)
        REFERENCES menu_scan(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    -- Indexes for query optimization
    INDEX idx_scan_id (scan_id),
    INDEX idx_ab_group (ab_group),
    INDEX idx_has_confidence (has_confidence),
    INDEX idx_ab_group_confidence (ab_group, has_confidence) COMMENT 'Composite index for analytics queries',
    INDEX idx_created_at (created_at),

    -- Constraints
    CONSTRAINT chk_survey_ab_group CHECK (ab_group IN ('CONTROL', 'TREATMENT')),
    CONSTRAINT uq_scan_response UNIQUE (scan_id) COMMENT 'One response per scan'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Survey responses for measuring user confidence (H1, H3 validation)';

-- ============================================================================
-- Initial Data (Optional - for testing)
-- ============================================================================

-- Uncomment below for local development seed data
-- INSERT INTO menu_scan (id, user_id, ab_group, source_language, target_language, source_currency, target_currency, created_at)
-- VALUES
--     (UNHEX(REPLACE(UUID(), '-', '')), 'test-user-1', 'CONTROL', 'en', 'ko', 'USD', 'KRW', NOW()),
--     (UNHEX(REPLACE(UUID(), '-', '')), 'test-user-2', 'TREATMENT', 'en', 'ko', 'USD', 'KRW', NOW());

-- ============================================================================
-- Verification Queries
-- ============================================================================

-- Verify table creation
-- SELECT TABLE_NAME, TABLE_COMMENT FROM information_schema.TABLES
-- WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME IN ('menu_scan', 'survey_response');

-- Verify indexes
-- SHOW INDEXES FROM menu_scan;
-- SHOW INDEXES FROM survey_response;

-- Verify FK constraint
-- SELECT
--     CONSTRAINT_NAME,
--     TABLE_NAME,
--     REFERENCED_TABLE_NAME
-- FROM information_schema.KEY_COLUMN_USAGE
-- WHERE TABLE_SCHEMA = DATABASE()
--     AND REFERENCED_TABLE_NAME IS NOT NULL
--     AND TABLE_NAME = 'survey_response';

-- ============================================================================
-- Rollback Script (if needed)
-- ============================================================================

-- DROP TABLE IF EXISTS survey_response;
-- DROP TABLE IF EXISTS menu_scan;

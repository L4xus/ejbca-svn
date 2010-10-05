-- Add rowVersion column to all tables
-- PublisherQueueData was a late add-on so we need to check if the column was created during appserver start-up
ALTER TABLE PublisherQueueData ADD COLUMN rowVersion INT4 DEFAULT 0;

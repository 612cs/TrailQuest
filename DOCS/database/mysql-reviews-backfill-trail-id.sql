-- 将历史回复评论的 trail_id 回填为其顶级评论所属路线
-- 适用于 MySQL 8+

WITH RECURSIVE review_lineage AS (
  SELECT
    id,
    parent_id,
    trail_id,
    id AS source_id,
    trail_id AS resolved_trail_id
  FROM reviews

  UNION ALL

  SELECT
    parent.id,
    parent.parent_id,
    parent.trail_id,
    lineage.source_id,
    COALESCE(lineage.resolved_trail_id, parent.trail_id) AS resolved_trail_id
  FROM review_lineage lineage
  JOIN reviews parent ON parent.id = lineage.parent_id
  WHERE lineage.resolved_trail_id IS NULL
),
resolved_reviews AS (
  SELECT
    source_id,
    MAX(resolved_trail_id) AS resolved_trail_id
  FROM review_lineage
  GROUP BY source_id
)
UPDATE reviews review_record
JOIN resolved_reviews resolved ON resolved.source_id = review_record.id
SET review_record.trail_id = resolved.resolved_trail_id
WHERE review_record.trail_id IS NULL
  AND resolved.resolved_trail_id IS NOT NULL;

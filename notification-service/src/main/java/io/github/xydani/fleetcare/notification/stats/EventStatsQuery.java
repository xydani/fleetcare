package io.github.xydani.fleetcare.notification.stats;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.DateOperators.DateToString;
import org.springframework.data.mongodb.core.aggregation.DateOperators.Timezone;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import io.github.xydani.fleetcare.notification.event.EventType;
import io.github.xydani.fleetcare.notification.stats.dto.DailyEventCount;

/**
 * Event counts per day and type, computed by MongoDB with an aggregation pipeline.
 */
@Repository
public class EventStatsQuery {

    private final MongoTemplate mongoTemplate;

    public EventStatsQuery(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Equivalent shell pipeline:
     * <pre>
     * db.event_log.aggregate([
     *   { $match:   { received_at: { $gte: since } } },
     *   { $project: { type: 1, day: { $dateToString: { format: "%Y-%m-%d", date: "$received_at", timezone: zone } } } },
     *   { $group:   { _id: { day: "$day", type: "$type" }, count: { $sum: 1 } } },
     *   { $sort:    { "_id.day": 1, "_id.type": 1 } }
     * ])
     * </pre>
     *
     * @param zone the time zone that decides which day an event belongs to
     */
    public List<DailyEventCount> countPerDayAndType(Instant since, ZoneId zone) {
        Aggregation pipeline = newAggregation(
                match(Criteria.where("received_at").gte(since)),
                project("type").and(DateToString.dateOf("received_at")
                        .toString("%Y-%m-%d")
                        .withTimezone(Timezone.valueOf(zone.getId()))).as("day"),
                group("day", "type").count().as("count"),
                sort(Sort.by("_id.day", "_id.type")));

        return mongoTemplate.aggregate(pipeline, "event_log", Document.class).getMappedResults().stream()
                .map(EventStatsQuery::toDailyCount)
                .toList();
    }

    private static DailyEventCount toDailyCount(Document row) {
        Document id = row.get("_id", Document.class);
        return new DailyEventCount(LocalDate.parse(id.getString("day")), EventType.valueOf(id.getString("type")),
                row.get("count", Number.class).longValue());
    }
}

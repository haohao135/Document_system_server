package com.document.demo.repository;

import com.document.demo.models.Documents;
import com.document.demo.models.Tracking;
import com.document.demo.models.User;
import com.document.demo.models.enums.TrackingActionType;
import com.document.demo.models.enums.TrackingEntityType;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TrackingRepository extends MongoRepository<Tracking, String> {
    List<Tracking> findByActor(User actor);
    List<Tracking> findByEntityTypeAndEntityId(TrackingEntityType type, String entityId);
    List<Tracking> findByAction(TrackingActionType action);
    List<Tracking> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    List<Tracking> findByEntityType(TrackingEntityType entityType);
    void deleteByTimestampBefore(LocalDateTime before);
    List<Tracking> findByActorAndTimestampBetween(
        User actor, 
        LocalDateTime start, 
        LocalDateTime end
    );
    @Query("{'entityType': ?0, 'timestamp': {$gte: ?1, $lte: ?2}}")
    List<Tracking> findByEntityTypeAndTimestampBetween(
        TrackingEntityType type,
        LocalDateTime start,
        LocalDateTime end
    );
    @Aggregation(pipeline = {
        "{'$match': {'entityType': ?0}}",
        "{'$group': {'_id': '$action', 'count': {'$sum': 1}}}"
    })
    List<Documents> countActionsByEntityType(TrackingEntityType entityType);
} 
package org.istiaqfuad.eventhub.event.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "events")
public class EventDocument {

    @Id
    private String id; // We will use the PostgreSQL Event ID as string

    @Field(type = FieldType.Keyword)
    private String publicId;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Keyword)
    private String categoryName;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Keyword)
    private List<String> tags;

    @Field(type = FieldType.Keyword)
    private String city;

    @GeoPointField
    private GeoPoint location; // For geospatial search (latitude, longitude)

    @Field(type = FieldType.Date)
    private OffsetDateTime startsAt;

    @Field(type = FieldType.Date)
    private OffsetDateTime endsAt;
    
    @Field(type = FieldType.Boolean)
    private Boolean highDemand;
}

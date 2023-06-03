package common.model.converter;

import common.model.TrackingStatus;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class TrackingStatusConverter implements AttributeConverter<TrackingStatus, Integer> {
    @Override
    public Integer convertToDatabaseColumn(TrackingStatus trackingStatus) {
        if (trackingStatus == null) {
            return null;
        }
        return trackingStatus.value();
    }

    @Override
    public TrackingStatus convertToEntityAttribute(Integer value) {
        if (value == null) {
            return null;
        }
        return TrackingStatus.fromInt(value);

//        return Stream.of(TrackingStatus.values())
//                .filter(c -> c.equals(value))
//                .findFirst()
//                .orElseThrow(IllegalArgumentException::new);
    }
}

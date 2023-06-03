package common.model.converter;

import common.model.PickingStatus;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class PickingStatusConverter implements AttributeConverter<PickingStatus, Integer> {
    @Override
    public Integer convertToDatabaseColumn(PickingStatus pickingStatus) {
        if (pickingStatus == null) {
            return null;
        }
        return pickingStatus.value();
    }

    @Override
    public PickingStatus convertToEntityAttribute(Integer value) {
        if (value == null) {
            return null;
        }

        return PickingStatus.fromInt(value);

//        return Stream.of(PickingStatus.values())
//                .filter(c -> c.equals(value))
//                .findFirst()
//                .orElseThrow(IllegalArgumentException::new);
    }
}

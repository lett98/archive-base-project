package common.service;


import common.model.PickingStatus;
import common.model.TrackingStatus;
import common.model.trk.CmTrk;
import common.vo.JobEmptyException;

public interface CMService {
    CmTrk selectPending(PickingStatus pickingStatus, TrackingStatus archivingStatus) throws JobEmptyException;
    CmTrk pick(CmTrk cmTrk, PickingStatus pickingStatus, TrackingStatus archivingStatus) throws Exception;

    CmTrk release(Long pickedJobId, PickingStatus pickingStatus, TrackingStatus archivingStatus);

}

package common.service;

import common.model.PickingStatus;
import common.model.SpringBatchJobStatus;
import common.model.TrackingStatus;
import common.model.trk.CmTrk;
import common.repository.trk.CmTrkRepository;
import common.vo.JobEmptyException;
import io.sentry.Sentry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CmServiceImpl implements CMService{
    @Autowired
    private CmTrkRepository cmTrkRepository;


    @Override
    public CmTrk selectPending(PickingStatus pickingStatus, TrackingStatus archivingStatus) throws JobEmptyException {
        Long jobIdPicked =  cmTrkRepository.findByStatuses(
                SpringBatchJobStatus.COMPLETED.value(),
                pickingStatus.value(),
                archivingStatus.value());
        if (jobIdPicked == null) {
            JobEmptyException error = new JobEmptyException("Cannot pick a job to process. The process is out.");
            Sentry.captureException(error);
            throw error;
        }
        return cmTrkRepository.findById(pickingStatus.value(), archivingStatus.value(), jobIdPicked);
    }

    @Override
    public CmTrk pick(CmTrk cmTrk, PickingStatus pickingStatus, TrackingStatus archivingStatus) throws Exception {
        int affected = cmTrkRepository.updateCmTrkWithVersion(
                pickingStatus.value(),
                cmTrk.getVersion() + 1,
                cmTrk.getJobId(),
                cmTrk.getVersion()
        );
        if (affected != 1) {
            throw new Exception("Row effected expected = 1, but got " + affected);
        }
        return cmTrkRepository
                .findByIdAndVersion(
                        pickingStatus.value(),
                        archivingStatus.value(),
                        cmTrk.getJobId(),
                        cmTrk.getVersion() + 1);
    }

    @Override
    public CmTrk release(Long pickedJobId, PickingStatus pickingStatus, TrackingStatus archivingStatus) {
        cmTrkRepository.updateCmTrk(pickingStatus.value(),archivingStatus.value(),pickedJobId);
        return cmTrkRepository.findById(pickingStatus.value(), archivingStatus.value(), pickedJobId);
    }
}

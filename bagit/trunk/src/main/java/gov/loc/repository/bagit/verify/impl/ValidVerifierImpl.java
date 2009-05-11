package gov.loc.repository.bagit.verify.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.CancelIndicator;
import gov.loc.repository.bagit.Cancellable;
import gov.loc.repository.bagit.ProgressIndicator;
import gov.loc.repository.bagit.ProgressMonitorable;
import gov.loc.repository.bagit.utilities.SimpleResult;
import gov.loc.repository.bagit.verify.CompleteVerifier;
import gov.loc.repository.bagit.verify.ManifestChecksumVerifier;
import gov.loc.repository.bagit.verify.ValidVerifier;

public class ValidVerifierImpl extends AbstractVerifier implements ValidVerifier, Cancellable, ProgressMonitorable {

	private static final Log log = LogFactory.getLog(ValidVerifierImpl.class);
	
	private CompleteVerifier completeVerifier;
	private ManifestChecksumVerifier manifestVerifier;
	private CancelIndicator cancelIndicator;
	
	@Override
	public void setCancelIndicator(CancelIndicator cancelIndicator) {
		this.cancelIndicator = cancelIndicator;
		if (completeVerifier instanceof Cancellable) {
			((Cancellable)completeVerifier).setCancelIndicator(cancelIndicator);
		}
		if (manifestVerifier instanceof Cancellable) {
			((Cancellable)manifestVerifier).setCancelIndicator(cancelIndicator);
		}		
	}
	
	@Override
	public void setProgressIndicator(ProgressIndicator progressIndicator) {
		if (completeVerifier instanceof ProgressMonitorable) {
			((ProgressMonitorable)completeVerifier).setProgressIndicator(progressIndicator);
		}
		if (manifestVerifier instanceof ProgressMonitorable) {
			((ProgressMonitorable)manifestVerifier).setProgressIndicator(progressIndicator);
		}
	}
	
	public ValidVerifierImpl(CompleteVerifier completeVerifier, ManifestChecksumVerifier manifestVerifier) {
		this.completeVerifier = completeVerifier;
		this.manifestVerifier = manifestVerifier;
	}
	
	@Override
	public SimpleResult verify(Bag bag) {
		//Is complete
		SimpleResult result = this.completeVerifier.verify(bag);
		if (cancelIndicator != null && cancelIndicator.performCancel()) return null;
		if (! result.isSuccess())
		{
			return result;
		}

		//Every checksum checks
		result = this.manifestVerifier.verify(bag.getTagManifests(), bag);
		if (cancelIndicator != null && cancelIndicator.performCancel()) return null;
		if (! result.isSuccess()) {
			return result;
		}

		result = this.manifestVerifier.verify(bag.getPayloadManifests(), bag);
		if (cancelIndicator != null && cancelIndicator.performCancel()) return null;
		if (! result.isSuccess()) {
			return result;
		}
		
		log.info("Validity check: " + result.toString());				
		return result;

	}

}
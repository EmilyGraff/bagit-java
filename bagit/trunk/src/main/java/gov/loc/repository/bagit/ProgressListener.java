package gov.loc.repository.bagit;

public interface ProgressListener {
	void reportProgress(String activity, String item, int count, int total);
}
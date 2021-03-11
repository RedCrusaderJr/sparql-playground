package swiss.sib.sparql.playground.repository.impl;

public enum RepositoryType {

	DEFAULT, NATIVE, MARK_LOGIC;

	public static RepositoryType getRepositoryType(String repositoryType) {

		if (repositoryType == "native") {
			return RepositoryType.NATIVE;

		} else if (repositoryType == "marklogic") {
			return RepositoryType.MARK_LOGIC;

		} else {
			return RepositoryType.DEFAULT;
		}
	}
}

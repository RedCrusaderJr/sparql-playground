package swiss.sib.sparql.playground.repository.impl;

public enum RepositoryType {

	DEFAULT, NATIVE, MARK_LOGIC;

	public static RepositoryType parseType(String repositoryType) {

		if (repositoryType.equals("default")) {
			return RepositoryType.DEFAULT;

		} else if (repositoryType.equals("native")) {
			return RepositoryType.NATIVE;

		} else if (repositoryType.equals("marklogic")) {
			return RepositoryType.MARK_LOGIC;

		} else {
			return RepositoryType.DEFAULT;
		}
	}
}

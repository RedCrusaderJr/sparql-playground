package swiss.sib.sparql.playground.geosparql.functions;

import swiss.sib.sparql.playground.geosparql.functions.parameters.IntersectFunctionParameters;

public class IntersectFunction implements GeosparqlFunction {

	IntersectFunctionParameters parameters;

	public IntersectFunction(GeosparqlFunctionParameters parameters) throws Exception {
		if (!(parameters instanceof IntersectFunctionParameters)) {
			throw new Exception("Wrong argument exception. Expected: IntersectFunctionParameters");
		}

		this.parameters = (IntersectFunctionParameters) parameters;
	}

	@Override
	public Object Execute() {
		// TODO Auto-generated method stub
		return null;
	}

}

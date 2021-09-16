var sem = require('/MarkLogic/semantics.xqy');
import { bufferFunction } from  '/buffer-function.mjs';

var query = `
SELECT DISTINCT *
WHERE {
    ?s ?p ?o.
}
`;

var params = {bFun: bufferFunction, intersectionFunction: geo.regionIntersects}
var results = sem.sparql(query,params);
results
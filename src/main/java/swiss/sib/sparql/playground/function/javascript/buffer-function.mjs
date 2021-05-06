import { BufferCreator } from  "/buffer-creator.mjs";

export function bufferFunction(lineStr, distanceArg) {
	if(lineStr == null || distanceArg == null) {
		throw new Error("One of arguments was null/undefined -> lineStr: " + lineStr + ", distanceArg: " + distanceArg);
	}

	let wktLinestringMatch = parseLineStr(lineStr);
	let x1 = wktLinestringMatch.groups.x1;
	let y1 = wktLinestringMatch.groups.y1;
	let x2 = wktLinestringMatch.groups.x2;
	let y2 = wktLinestringMatch.groups.y2;

	if(isNaN(distanceArg))	{
		throw new Error("Not a number: " + distanceArg);
	}

	let bufferCreator = new BufferCreator(x1, y1, x2, y2, distanceArg);
	return bufferCreator.evaluate();
}

function parseLineStr(lineStr) {
	// LINESTRING (x1 y1, x2 y2)
	let wktLinestringRegex = /LINESTRING \((?<x1>.*) (?<y1>.*), (?<x2>.*) (?<y2>.*)\)/;
	let wktLinestringMatch = lineStr.match(wktLinestringRegex);

	if (isNaN(wktLinestringMatch.groups.x1) ||
		isNaN(wktLinestringMatch.groups.y1) ||
		isNaN(wktLinestringMatch.groups.x2) ||
		isNaN(wktLinestringMatch.groups.y2)) {
		throw new Error("invalid argument format: " + lineStr + " [wkt linestring expected: 'LINESTRING (x1 y1, x2 y2)']");
	}

	return wktLinestringMatch;
}

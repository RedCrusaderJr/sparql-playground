import CrsTransformator from '/crs-transformator.mjs';

const BigNumber = require('/bignumber.js');
const Proj4js = require('/proj4js.js');

var transformator;

export default class BufferCreator {
    constructor(inputLongitude1, inputLatitude1, inputLongitude2, inputLatitude2, distanceTotal) {	
		try {
			transformator = new CrsTransformator();

			let point1 = Proj4js.toPoint([inputLongitude1, inputLatitude1]);
			let point2 = Proj4js.toPoint([inputLongitude2, inputLatitude2]);

			let utmPoint1 = transformator.fromWgs84ToUtmZone10N(point1);
			let utmPoint2 = transformator.fromWgs84ToUtmZone10N(point2);

			this.utmEasting1 = new BigNumber(utmPoint1.x);
			this.utmNorthing1 = new BigNumber(utmPoint1.y);
			this.utmEasting2 = new BigNumber(utmPoint2.x);
			this.utmNorthing2 = new BigNumber(utmPoint2.y);
			this.distanceTotal = new BigNumber(distanceTotal);

			this.bufferCreationMethod = this.getBufferCreationMethod();

		} catch (error) {
			return "" + error;
		}
    }

    evaluate() {
		let strResult = "POINT EMPTY";

		try {
			strResult = this.bufferCreationMethod();

		} catch (error) {
			return "" + error;
		}

		return strResult;
    }

	getBufferCreationMethod() {
		if (this.utmEasting2.isEqualTo(this.utmEasting1)) {
			return this.equalEastingsBufferCreation;
		}
		
		if (this.utmNorthing2.isEqualTo(this.utmNorthing1)) {
			return this.equalNorthingBufferCreation;
		}
		
		return this.basicCaseBufferCreation;
	}
	
	// CASE x1 = x2
	equalEastingsBufferCreation() {
		// correcting the order
		if (this.utmNorthing2.isLessThan(this.utmNorthing1)) {
			let temp = this.utmNorthing2;
			this.utmNorthing2 = this.utmNorthing1;
			this.utmNorthing1 = temp;
		}
	
		// POLYGONE POINTS
		
		// point1.x = utmEasting1 - distanceTotal
		// point1.y = utmNorthing1 - distanceTotal
		let point1 = new Proj4js.toPoint([this.utmEasting1.minus(this.distanceTotal).toNumber(), this.utmNorthing1.minus(this.distanceTotal).toNumber()]);
	
		// point2.x = utmEasting1 + distanceTotal
		// point2.y = utmNorthing1 - distanceTotal
		let point2 = new Proj4js.toPoint([this.utmEasting1.plus(this.distanceTotal).toNumber(), this.utmNorthing1.minus(this.distanceTotal).toNumber()]);
	
		// point3.x = utmEasting2 + distanceTotal
		// point3.y = utmNorthing2 + distanceTotal
		let point3 = new Proj4js.toPoint([this.utmEasting2.plus(this.distanceTotal).toNumber(), this.utmNorthing2.plus(this.distanceTotal).toNumber()]);
	
		// point4.x = utmEasting2 - distanceTotal
		// point4.y = utmNorthing2 + distanceTotal
		let point4 = new Proj4js.toPoint([this.utmEasting2.minus(this.distanceTotal).toNumber(), this.utmNorthing2.plus(this.distanceTotal).toNumber()]);
	
		return formatPolygonStr(point1, point2, point3, point4);
	}
	
	// CASE y1 = y2
	equalNorthingBufferCreation() {
		// correcting the order
		if (this.utmEasting2.isLessThan(this.utmEasting1)) {
			let temp = this.utmEasting2;
			this.utmEasting2 = this.utmEasting1;
			this.utmEasting1 = temp;
		}
	
		// POLYGONE POINTS
	
		// point1.x = utmEasting1 - distanceTotal
		// point1.y = utmNorthing1 - distanceTotal
		let point1 = new Proj4js.toPoint([this.utmEasting1.minus(this.distanceTotal).toNumber(), this.utmNorthing1.minus(this.distanceTotal).toNumber()]);
	
		// point2.x = utmEasting1 - distanceTotal
		// point2.y = utmNorthing1 + distanceTotal
		let point2 = new Proj4js.toPoint([this.utmEasting1.minus(this.distanceTotal).toNumber(), this.utmNorthing1.plus(this.distanceTotal).toNumber()]);
	
		// point3.x = utmEasting2 + distanceTotal
		// point3.y = utmNorthing2 + distanceTotal
		let point3 = new Proj4js.toPoint([this.utmEasting2.plus(this.distanceTotal).toNumber(), this.utmNorthing2.plus(this.distanceTotal).toNumber()]);
	
		// point4.x = utmEasting2 + distanceTotal
		// point4.y = utmNorthing2 - distanceTotal
		let point4 = new Proj4js.toPoint([this.utmEasting2.plus(this.distanceTotal).toNumber(), this.utmNorthing2.minus(this.distanceTotal).toNumber()]);
	
		return formatPolygonStr(point1, point2, point3, point4);
	}
	
	// CASE x1 != x2 && y1 != y2
	basicCaseBufferCreation() {
		//slope of acline segment to x-axis
		let slope = calculateSlope(this.utmEasting1, this.utmNorthing1, this.utmEasting2, this.utmNorthing2);
		this.correctCoordinatesOrder(slope.isPositive());
	
		this.extendLine(slope);
		return this.createPolygonStr(slope);
	}

	// puts coordinates in right order
	correctCoordinatesOrder(isSharp) {
		let correctionNeeded = false;

		if (this.utmEasting1.isGreaterThan(this.utmEasting2)) {
			correctionNeeded = true;
		}

		if (isSharp && this.utmNorthing1.isGreaterThan(this.utmNorthing2)) {
			correctionNeeded = true;
		}

		if (!isSharp && this.utmNorthing1.isLessThan(this.utmNorthing2)) {
			correctionNeeded = true;
		}

		if (correctionNeeded) {
			let temp = this.utmEasting1;
			this.utmEasting1 = this.utmEasting2;
			this.utmEasting2 = temp;

			temp = this.utmNorthing1;
			this.utmNorthing1 = this.utmNorthing2;
			this.utmNorthing2 = temp;
		}
	}

	// extends the line for distanceTotal at each end
	extendLine(slope) {
		let verticalOffset = calculateVerticalOffset(slope, this.distanceTotal);
		let horizontalOffset = calculateHorizontalOffset(verticalOffset, slope);

		// utmEasting1 = utmEasting1 - horizontalOffset
		// utmNorthing1 = utmNorthing1 - verticalOffset
		this.utmEasting1 = this.utmEasting1.minus(horizontalOffset);
		this.utmNorthing1 = this.utmNorthing1.minus(verticalOffset);

		// utmEasting2 = utmEasting2 + horizontalOffset
		// utmNorthing2 = utmNorthing2 + verticalOffset
		this.utmEasting2 = this.utmEasting2.plus(horizontalOffset);
		this.utmNorthing2 = this.utmNorthing2.plus(verticalOffset);
	}

	createPolygonStr(slope) {
		let pSlope = calculatePerpendicularSlope(slope);
		let verticalOffset = calculateVerticalOffset(pSlope, this.distanceTotal);
		let horizontalOffset = calculateHorizontalOffset(verticalOffset, pSlope);
	
		// point1.x = utmEasting1 - horizontalOffset
		// point1.y = utmNorthing1 - verticalOffset
		let point1 = Proj4js.toPoint([this.utmEasting1.minus(horizontalOffset).toNumber(), this.utmNorthing1.minus(verticalOffset).toNumber()]);
	
		// point2.x = utmEasting1 + horizontalOffset
		// point2.y = utmNorthing1 + verticalOffset
		let point2 = Proj4js.toPoint([this.utmEasting1.plus(horizontalOffset).toNumber(), this.utmNorthing1.plus(verticalOffset).toNumber()]);
	
		// point3.x = utmEasting2 + horizontalOffset
		// point3.y = utmNorthing2 + verticalOffset
		let point3 = Proj4js.toPoint([this.utmEasting2.plus(horizontalOffset).toNumber(), this.utmNorthing2.plus(verticalOffset).toNumber()]);
	
		// point4.x = utmEasting2 - horizontalOffset
		// point4.y = utmNorthing2 - verticalOffset
		let point4 = Proj4js.toPoint([this.utmEasting2.minus(horizontalOffset).toNumber(), this.utmNorthing2.minus(verticalOffset).toNumber()]);
	
		return formatPolygonStr(point1, point2, point3, point4);
	}
}

function formatPolygonStr(utmPoint1, utmPoint2, utmPoint3, utmPoint4) {	
	let wgsPoint1 = transformator.fromUtmZone10NToWgs84(utmPoint1);
	let wgsPoint2 = transformator.fromUtmZone10NToWgs84(utmPoint2);
	let wgsPoint3 = transformator.fromUtmZone10NToWgs84(utmPoint3);
	let wgsPoint4 = transformator.fromUtmZone10NToWgs84(utmPoint4);
	
	let str = "";
	str = str.concat("POLYGON ((")
			.concat(wgsPoint1.x).concat(" ").concat(wgsPoint1.y).concat(", ")
			.concat(wgsPoint2.x).concat(" ").concat(wgsPoint2.y).concat(", ")
			.concat(wgsPoint3.x).concat(" ").concat(wgsPoint3.y).concat(", ")
			.concat(wgsPoint4.x).concat(" ").concat(wgsPoint4.y).concat(", ")
			.concat(wgsPoint1.x).concat(" ").concat(wgsPoint1.y).concat("))");
	return str;
}

// x2 and x1 must not not be equal
function calculateSlope(x1, y1, x2, y2) {
	//slope = (y2 - y1) / (x2 - x1)
	return (y2.minus(y1)).dividedBy(x2.minus(x1));
}

//slope of perpendicular line
//eastings must not be equal
function calculatePerpendicularSlope(slope) {
	//pSlope = - 1 / slope
	return new BigNumber(-1).dividedBy(slope);
}

function calculateVerticalOffset(slope, distanceTotal) {
	// angle of line to x-axis [rad]: angle = arcus tangent(slope)
	let angle = new BigNumber(Math.atan(slope));
	if (angle.isZero() || angle.isNaN()) {
		throw new Error("angle: " + angle);
	}

	// verticalOffset = distanceTotal * sin(angle)
	let angleSin = new BigNumber(Math.sin(angle));
	return distanceTotal.multipliedBy(angleSin);
}

// slope must not be zero (latitudes/northings must not equal)
function calculateHorizontalOffset(verticalOffset, slope) {
	// horizontalOffset = verticalOffeset / slope
	return verticalOffset.dividedBy(slope); 
}

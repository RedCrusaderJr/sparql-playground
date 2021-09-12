const BigNumber = require('/bignumber.js');

export class BufferCreator {
    constructor(x1, y1, x2, y2, distanceTotal) {
		this.latUnit = new BigNumber(111200.0);
		this.lonUnit = new BigNumber(78630.0);

		this.x1 = new BigNumber(x1);
		this.y1 = new BigNumber(y1);
		this.x2 = new BigNumber(x2);
		this.y2 = new BigNumber(y2);
		this.distanceTotal = new BigNumber(distanceTotal);
    }

    evaluate() {
		var strResult = "";
		if (this.x2.isEqualTo(this.x1)) {
			strResult = this.equalLatitudesSpecialCase();

		} else if (this.y2.isEqualTo(this.y1)) {
			strResult = this.equalLongitudesSpecialCase();

		} else {
			strResult = this.basicCase();
		}

		return strResult;
    }

    // CASE x1 = x2
    equalLatitudesSpecialCase() {
		if (this.y2.isLessThan(this.y1)) {
			let temp = this.y2;
			this.y2 = this.y1;
			this.y1 = temp;
		}

		// TODO: UMT conversion
		// latitude change [deg]: latDeg = distanceTotal / latUnit
		let latDeg = this.distanceTotal.dividedBy(this.latUnit); // latUnit will never be zero
		// longitude change [deg]: lonDeg = distanceTotal / lonUnit
		let lonDeg = this.distanceTotal.dividedBy(this.lonUnit); // lonUnit will never be zero

		// POLYGONE POINTS

		// pX1 = x1 - lonDeg
		// pY1 = y1 - latDeg
		let pX1 = this.x1.minus(lonDeg);
		let pY1 = this.y1.minus(latDeg);

		// pX2 = x1 + lonDeg
		// pY2 = y1 - latDeg
		let pX2 = this.x1.plus(lonDeg);
		let pY2 = this.y1.minus(latDeg);

		// pX3 = x2 + lonDeg
		// pY3 = y2 + latDeg
		let pX3 = this.x2.plus(lonDeg);
		let pY3 = this.y2.plus(latDeg);

		// pX4 = x2 - lonDeg
		// pY4 = y2 + latDeg
		let pX4 = this.x2.minus(lonDeg);
		let pY4 = this.y2.plus(latDeg);

		return this.formatPolygonStr(pX1, pY1, pX2, pY2, pX3, pY3, pX4, pY4);
    }

    // CASE y1 = y2
    equalLongitudesSpecialCase() {
		if (this.x2.isLessThan(this.x1)) {
			let temp = this.x2;
			this.x2 = this.x1;
			this.x1 = temp;
		}

		// TODO: UMT conversion
		// latitude change [deg]: latDeg = distanceTotal / latUnit
		let latDeg = this.distanceTotal.dividedBy(this.latUnit); // latUnit will never be zero
		// longitude change [deg]: lonDeg = distanceTotal / lonUnit
		let lonDeg = this.distanceTotal.dividedBy(this.lonUnit); // lonUnit will never be zero

		// POLYGONE POINTS

		// pX1 = x1 - lonDeg
		// pY1 = y1 - latDeg
		let pX1 = this.x1.minus(lonDeg);
		let pY1 = this.y1.minus(latDeg);

		// pX2 = x1 - lonDeg
		// pY2 = y1 + latDeg
		let pX2 = this.x1.minus(lonDeg);
		let pY2 = this.y1.plus(latDeg);

		// pX3 = x2 + lonDeg
		// pY3 = y2 + latDeg
		let pX3 = this.x2.plus(lonDeg);
		let pY3 = this.y2.plus(latDeg);

		// pX4 = x2 + lonDeg
		// pY4 = y2 - latDeg
		let pX4 = this.x2.plus(lonDeg);
		let pY4 = this.y2.minus(latDeg);

		return this.formatPolygonStr(pX1, pY1, pX2, pY2, pX3, pY3, pX4, pY4);
    }

    // CASE x1 != x2 && y1 != y2
    basicCase() {
      	// slope of acline segment: slope = (y2 - y1) / (x2 - x1)
		let slope = this.y2.minus(this.y1).dividedBy(this.x2.minus(this.x1)); // x2 and x1 will not be equal
		let isSharp = slope.isPositive();

      	// puts coordinates in right order
		this.correctCoordOrder(isSharp);
		// extends line for distanceTotal length at each end
		this.extendLine(slope);

		return this.createPolygonStr(slope);
    }

    correctCoordOrder(isSharp) {
		let correctionNeeded = false;

		if (this.x1.isGreaterThan(this.x2)) {
			correctionNeeded = true;
		}

		if (isSharp && this.y1.isGreaterThan(this.y2)) {
			correctionNeeded = true;
		}

		if (!isSharp && this.y1.isLessThan(this.y2)) {
			correctionNeeded = true;
		}

		if (correctionNeeded) {
			let temp = this.x1;
			this.x1 = this.x2;
			this.x2 = temp;

			temp = this.y1;
			this.y1 = this.y2;
			this.y2 = temp;
		}
    }

    // extends the line for distanceTotal at each end
    extendLine(slope) {
		// angle of line to x-axis [rad]: pAngle = arcus tangent(pSlope)
		let angle = new BigNumber(Math.atan(slope));
		if (angle.isZero() || angle.isNaN()) {
			throw new Error("perpendicular angle: " + angle.doubleValue());
		}

		// X and Y components of total distance [m]:
		// distanceY = distanceTotal * sin(angle)
		let angleSin = new BigNumber(Math.sin(angle));
		let distanceY = this.distanceTotal.multipliedBy(angleSin);
		// distanceX = distanceY / pSlope
		let distanceX = distanceY.dividedBy(slope); // slope will not be zero if latitudes are not equal

		// TODO: UMT conversion
		// latitude change [deg]
		let latDeg = distanceX.dividedBy(this.latUnit); // latUnit will never be zero
		// longitude change [deg]
		let lonDeg = distanceY.dividedBy(this.lonUnit); // lonUnit will never be zero

		// x1 = x1 - lonDeg
		// y1 = y1 - latDeg
		this.x1 = this.x1.minus(lonDeg);
		this.y1 = this.y1.minus(latDeg);

		// x2 = x2 + lonDeg
		// y2 = y2 + latDeg
		this.x2 = this.x2.plus(lonDeg);
		this.y2 = this.y2.plus(latDeg);
    }

    createPolygonStr(slope) {
      	// slope of perpendicular line: pSlope = - 1 / Slope
		let pSlope = new BigNumber(-1).dividedBy(slope); // slope will not be zero if longitudes are not equal

		// angle of perpendicular line to x-axis [rad]: pAngle = arcus tangent(pSlope)
		let pAngle = new BigNumber(Math.atan(pSlope));
		if (pAngle.isZero() || pAngle.isNaN()) {
			throw new Error("perpendicular angle: " + pAngle.doubleValue());
		}

		// X and Y components of total distance [m]:
		// distanceY = distanceTotal * sin(angle)
		let angleSin = new BigNumber(Math.sin(pAngle));
		let distanceY = this.distanceTotal.multipliedBy(angleSin);
		// distanceX = distanceY / pSlope
		let distanceX = distanceY.dividedBy(pSlope); // slope will not be zero if latitudes are not equal

		// TODO: UMT conversion
		// latitude change [deg]
		let latDeg = distanceX.dividedBy(this.latUnit); // latUnit will never be zero
		// longitude change [deg]
		let lonDeg = distanceY.dividedBy(this.lonUnit); // lonUnit will never be zero

		// pX1 = x1 - lonDeg
		// pY1 = y1 - latDeg
		let pX1 = this.x1.minus(lonDeg);
		let pY1 = this.y1.minus(latDeg);

		// pX2 = x1 + lonDeg
		// pY2 = y1 + latDeg
		let pX2 = this.x1.plus(lonDeg);
		let pY2 = this.y1.plus(latDeg);

		// pX3 = x2 + lonDeg
		// pY3 = y2 + latDeg
		let pX3 = this.x2.plus(lonDeg);
		let pY3 = this.y2.plus(latDeg);

		// pX4 = x2 - lonDeg
		// pY4 = y2 - latDeg
		let	pX4 = this.x2.minus(lonDeg);
		let pY4 = this.y2.minus(latDeg);

		return this.formatPolygonStr(pX1, pY1, pX2, pY2, pX3, pY3, pX4, pY4);
    }

    formatPolygonStr(pX1, pY1, pX2, pY2, pX3, pY3, pX4, pY4) {
		let str = "";
		str = str.concat("POLYGON ((")
				.concat(pX1).concat(" ").concat(pY1).concat(", ")
				.concat(pX2).concat(" ").concat(pY2).concat(", ")
				.concat(pX3).concat(" ").concat(pY3).concat(", ")
				.concat(pX4).concat(" ").concat(pY4).concat(", ")
				.concat(pX1).concat(" ").concat(pY1).concat("))");
		return str;
    }
}

// (TF012****)
//const de9imRegex = /\((?<de9im>(T|F|0|1|2|\*){9})\)/;
const de9imRegex = /(?<de9im>(T|F|0|1|2|\*){9})/;

export default class De9imHelper {
    parse(patternMatrix, allowedCharsArray) {
        if(!patternMatrix) return "patternMatrix is not defined";

        let de9imMatch = patternMatrix.match(de9imRegex);
        if (!de9imMatch) return "de9imMatch is not defined";

        if (!allowedCharsArray) return parseBase(de9imMatch);
        else return parseWithAllowedChars(de9imMatch, allowedCharsArray);
    }
}

function parseBase(de9imMatch) {
    let tfArray = new Array(); 

    for (let i = 0; i < de9imMatch.groups.de9im.length; i++) {
        tfArray[i] = tfConversion(de9imMatch.groups.de9im[i], [])
    }

    return tfArray;
}

function parseWithAllowedChars(de9imMatch, allowedCharsArray) {
    if (allowedCharsArray.length != 9) return;

    let tfArray = new Array(); 

    for (let i = 0; i < de9imMatch.groups.de9im.length; i++) {
        tfArray[i] = tfConversion(de9imMatch.groups.de9im[i], allowedCharsArray[i])
    }

    return tfArray;
}

function tfConversion(char, allowedChars) {
    switch (char) {
        case 'T':
            return char;
        
        case 'F':
            return char;

        case '*':
            return char;

        case '0':
        case '1':
        case '2':
            if (contains(allowedChars, char)) return char;
            return 'T';

        default:
            return char;
    }
}

function contains(array, char) {
    for (let i = 0; i < array.length; i++) {
        if (array[i] == char) return true;
    }

    return false;
}
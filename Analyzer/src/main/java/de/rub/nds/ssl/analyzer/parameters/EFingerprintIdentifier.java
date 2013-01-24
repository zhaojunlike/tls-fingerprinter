package de.rub.nds.ssl.analyzer.parameters;

/**
 * Identifiers for tests.
 * @author Eugen Weiss - eugen.weiss@ruhr-uni-bochum.de
 * @version 0.1
 * Aug 2, 2012
 */
public enum EFingerprintIdentifier {	
        GoodCase,
	ClientHello,
	CHRecordHeader,
	CHHandshakeHeader,
	ClientKeyExchange,
	CKERecordHeader,
	CKEHandshakeHeader,
	ChangeCipherSpec,
	CCSRecordHeader,
	Finished,
	FinRecordHeader,
	FinHandshakeHeader,
	CheckHandEnum,
	BleichenbacherAttack
}
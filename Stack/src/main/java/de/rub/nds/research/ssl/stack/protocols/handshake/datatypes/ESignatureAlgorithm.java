package de.rub.nds.research.ssl.stack.protocols.handshake.datatypes;

/**
 * Algorithm used for signing
 * @author Eugen Weiss - eugen.weiss@rub.de
 * @version 0.1
 * Mar 10, 2012
 */
public enum ESignatureAlgorithm {
	RSA,
	DSS,
	anon;
}
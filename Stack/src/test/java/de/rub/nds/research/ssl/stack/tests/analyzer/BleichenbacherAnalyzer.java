package de.rub.nds.research.ssl.stack.tests.analyzer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import de.rub.nds.research.ssl.stack.protocols.alert.Alert;
import de.rub.nds.research.ssl.stack.protocols.commons.EProtocolVersion;
import de.rub.nds.research.ssl.stack.tests.analyzer.common.AFingerprintAnalyzer;
import de.rub.nds.research.ssl.stack.tests.analyzer.common.ETLSImplementation;
import de.rub.nds.research.ssl.stack.tests.analyzer.counter.ScoreCounter;
import de.rub.nds.research.ssl.stack.tests.analyzer.db.Database;
import de.rub.nds.research.ssl.stack.tests.trace.Trace;

public class BleichenbacherAnalyzer extends AFingerprintAnalyzer {
	
	private BleichenbacherParameters parameters = new BleichenbacherParameters();
	
	public BleichenbacherAnalyzer(byte [] mode, byte [] separate,
			EProtocolVersion protocolVersion, boolean changePadding,
			int position){
		parameters.setMode(mode);
		parameters.setSeparate(separate);
		parameters.setProtocolVersion(protocolVersion);
		parameters.setChangePadding(changePadding);
		parameters.setPosition(position);
	}

	@Override
	public void analyze(ArrayList<Trace> traceList) {
		Trace lastTrace = traceList.get(traceList.size()-1);
		String lastState = lastTrace.getState().name();
		String alertDesc = null;
		String stateBeforeAlert = null;
		if (lastState.equals("ALERT")) {
			Alert alert = (Alert) lastTrace.getCurrentRecord();
			alertDesc = alert.getAlertDescription().name();
			Trace previousTrace = traceList.get(traceList.size()-2);
			stateBeforeAlert = previousTrace.getState().name();
		}
		ScoreCounter counter = ScoreCounter.getInstance();
		String fingerprint = parameters.computeFingerprint();
		Database db = Database.getInstance();
		ResultSet result = db.checkFingerprintInDB(fingerprint);
		try {
			while (result.next()) {
				if (result.getString("LAST_STATE").equalsIgnoreCase("ALERT")) {
					if (result.getString("STATE_BEFORE_ALERT").equalsIgnoreCase(stateBeforeAlert) &&
							result.getString("ALERT").equalsIgnoreCase(alertDesc)) {
						counter.countResult(ETLSImplementation.valueOf(result.getString("TLS_IMPL")),
								result.getInt("POINTS"));
					}
				}
				else if (result.getString("LAST_STATE").equalsIgnoreCase(lastState)) {
					counter.countResult(ETLSImplementation.valueOf(result.getString("TLS_IMPL")),
							result.getInt("POINTS"));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
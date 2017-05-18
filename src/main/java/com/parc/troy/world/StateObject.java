package com.parc.troy.world;

import com.parc.troy.SoarInterface;

import sml.Identifier;

/**
 * This is an interface for maintaining state as well as writing and reading information from a Soar
 * agent. 
 * 
 * @author smohan
 *
 */
public interface StateObject {
	public void update();

	public void writeToSoar(Identifier stateId);

	public void readSoarCommandAndApply(Identifier commandId,
			SoarInterface soarI);
}

package com.parc.troy.world;

import java.util.HashMap;
import java.util.Map;

import sml.Identifier;

import com.parc.troy.SoarInterface;



public class World {
		
		private Map<Identifier, StateObject> stateObjects;
		private SoarInterface soarI;
		
		public World(String dmName, SoarInterface soarI, Identifier worldLink) {
			this.stateObjects = new HashMap<Identifier, StateObject>();
			this.soarI = soarI;
			
			this.addStateObjects(dmName, worldLink);
		}
		
		private void addStateObjects(String dmName, Identifier worldLink){
			this.stateObjects.put(worldLink, new LocalFileCollectionStateObject(dmName));
			
		}
		
		public void update(){
			for(Map.Entry<Identifier, StateObject> entry : this.stateObjects.entrySet()) {
				StateObject stateObject = entry.getValue();
				stateObject.update();
			}
		}
		
		public void writeToSoar(){
			for(Map.Entry<Identifier, StateObject> entry : this.stateObjects.entrySet()) {
				StateObject stateObject = entry.getValue();
				Identifier stateId = entry.getKey();
				stateObject.writeToSoar(stateId);
			}
		}
		
		public void readSoarOutputAndApply(Identifier commandId){
			for (Map.Entry<Identifier, StateObject> entry : this.stateObjects.entrySet()){
				StateObject stateObject = entry.getValue();
				stateObject.readSoarCommandAndApply(commandId, soarI);
			}
		}
	}
	
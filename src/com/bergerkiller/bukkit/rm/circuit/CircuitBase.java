package com.bergerkiller.bukkit.rm.circuit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;

import com.bergerkiller.bukkit.rm.RedstoneMania;
import com.bergerkiller.bukkit.rm.Util;
import com.bergerkiller.bukkit.rm.element.Inverter;
import com.bergerkiller.bukkit.rm.element.Port;
import com.bergerkiller.bukkit.rm.element.Redstone;
import com.bergerkiller.bukkit.rm.element.Repeater;

public class CircuitBase {
		
	public String name;
	public Redstone[] elements;
	public CircuitInstance[] subcircuits;
	private HashMap<String, Port> ports = new HashMap<String, Port>();
	private boolean initialized = false;

	public void doPhysics() {
		for (Redstone r : this.elements) {
			r.updateTick();
		}
		for (CircuitInstance ci : this.subcircuits) {
			ci.doPhysics();
		}
	}
	
	public void initialize() {
		this.initialize(true);
	}
	private void initialize(boolean generateIds) {
		this.ports.clear();
		for (Redstone r : this.elements) {
			r.setCircuit(this);
			r.onPowerChange();
			if (r instanceof Port) {
				this.ports.put(((Port) r).name, (Port) r);
			}
		}
		for (CircuitBase c : this.subcircuits) {
			c.initialize(false);
		}
		if (generateIds) {
			this.generateIds(0);
		}
		this.initialized = true;
	}
	public boolean isInitialized() {
		return this.initialized;
	}
	
	public Port getPort(String name) {
		return this.ports.get(name);
	}
	public Collection<Port> getPorts() {
		return this.ports.values();
	}
	
	public Redstone getElement(int id) {
		if (id >= this.elements.length) {
			for (CircuitBase sub : this.subcircuits) {
				for (Redstone r : sub.elements) {
					if (r.getId() == id) return r;
				}
			}
			return null;
		} else {
			return this.elements[id];
		}
	}
	public Redstone getElement(Redstone guide) {
		return this.getElement(guide.getId());
	}
	public void removeElement(Redstone element) {
		for (int i = 0; i < this.elements.length; i++) {
			if (this.elements[i] == element) {
				this.removeElement(i);
				return;
			}
		}
	}
	private void removeElement(int index) {
		Redstone[] newElements = new Redstone[this.elements.length - 1];
		for (int i = 0; i < index; i++) {
			newElements[i] = this.elements[i];
		}
		for (int i = index; i < newElements.length; i++) {
			newElements[i] = this.elements[i + 1];
		}
		this.elements = newElements;
	}
	
	public void log() {
		this.log(0);
	}
	public void log(int indent) {
		RedstoneMania.log(Level.INFO, Util.getIndent(indent) + "Logging circuit: " + this.getFullName());
		for (Redstone r : this.elements) {
			RedstoneMania.log(Level.INFO, r.toString()); 
			if (r.inputs.size() > 0) {
				Util.logElements(Util.getIndent(indent + 1) + "Receives input from: ", " | ", 150, r.inputs.toArray(new Object[0]));
			}
			if (r.outputs.size() > 0) {
				Util.logElements(Util.getIndent(indent + 1) + "Supplies output for: ", " | ", 150, r.outputs.toArray(new Object[0]));
			}
		}
		for (CircuitBase cb : this.subcircuits) {
			cb.log(indent + 1);
		}
	}
	
	private int generateIds(int startindex) {
		for (Redstone r : this.elements) {
			r.setId(startindex);
			startindex++;
		}
		for (CircuitBase cb : this.subcircuits) {
			startindex = cb.generateIds(startindex);
		}
		return startindex;
	}
	
	public File getFile() {
		//to be overridden
		return null;
	}
	public String getFullName() {
		return this.name;
	}
	public final boolean isSaved() {
		return this.getFile().exists();
	}
	
	public final boolean load() {
		return this.load(this.getFile());
	}
	public final boolean save() {
		return this.save(this.getFile());
	}
	
	public final boolean load(File file) {
		DataInputStream dis = null;
		boolean succ = false;
		try {
			dis = new DataInputStream(new FileInputStream(file));
			try {
				this.load(dis);
				succ = true;
			} catch (IOException ex) {
				RedstoneMania.log(Level.SEVERE, "Error while reading data: " + file.getName());
				ex.printStackTrace();
			} catch (Exception ex) {
				RedstoneMania.log(Level.SEVERE, "Error while loading data: " + file.getName());
				ex.printStackTrace();
			}
			dis.close();
		} catch (FileNotFoundException ex) {
			RedstoneMania.log(Level.WARNING, "Circuit not found: " + file.getName());
			ex.printStackTrace();
		} catch (IOException ex) {
			RedstoneMania.log(Level.SEVERE, "Failed to load circuit: " + file.getName());
		}
		return succ;
	}
	public final boolean save(File file) {
		DataOutputStream dos = null;
		boolean succ = false;
		try {
			dos = new DataOutputStream(new FileOutputStream(file));
			try {
				this.save(dos);
				succ = true;
			} catch (IOException ex) {
				RedstoneMania.log(Level.SEVERE, "Error while writing data: " + file.getName());
				ex.printStackTrace();
			} catch (Exception ex) {
				RedstoneMania.log(Level.SEVERE, "Error while saving data: " + file.getName());
				ex.printStackTrace();
			}
			dos.close();
		} catch (FileNotFoundException ex) {
			RedstoneMania.log(Level.WARNING, "Circuit not accessible: " + file.getName());
			ex.printStackTrace();
		} catch (IOException ex) {
			RedstoneMania.log(Level.SEVERE, "Failed to save circuit: " + file.getName());
			ex.printStackTrace();
		}
		return succ;
	}
	
	public void load(DataInputStream stream) throws Exception {
		//to be overridden
	}
	public void save(DataOutputStream stream) throws Exception {
		//to be overridden
	}

}
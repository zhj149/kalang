package kava.compiler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import kava.opcode.VarObject;

public class VarTable {
	
	private HashMap<Integer,VarObject> vars = new HashMap(); //id => var
	private HashMap<String,Integer> ids = new HashMap();//name -> id
	private int tmpIdx = 0;
	private int varIdx = 0;
	
	public Collection<VarObject> getVars(){
		return this.vars.values();
	}
	
	public VarObject get(Integer idx){
		return vars.get(idx);
	}
	public VarObject get(String name){
		Integer id = ids.get(name);
		return get(id);
	}
	public boolean contains(String name){
		return ids.containsKey(name);
	}
	
	
	public VarObject createTmp(){
		return create("@"+(tmpIdx++));
	}
	public VarObject create(String name){
		VarObject vo = new VarObject();
		Integer id = varIdx++;
		vo.setId(id);
		vo.setName(name);
		vars.put(id,vo);
		ids.put(name,id);
		return vo;
	}
		
	public String toString(){
		String str = "";
		for(Entry<String, Integer> e:this.ids.entrySet()){
			VarObject vo = vars.get(e.getValue());
			str += String.format("%s(%d):%s",e.getKey(),e.getValue(),vo) + "\n";
		}
		return str;
	}
}

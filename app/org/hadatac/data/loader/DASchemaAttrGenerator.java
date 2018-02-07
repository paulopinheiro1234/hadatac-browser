package org.hadatac.data.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import java.lang.String;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVRecord;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.utils.ConfigProp;

public class DASchemaAttrGenerator extends BasicGenerator {

	final String kbPrefix = ConfigProp.getKbPrefix();
	String startTime = "";
	String SDDName = "";
	Map<String, String> codeMap;
	Map<String, List<String>> hasEntityMap = new HashMap<String, List<String>>();

	public DASchemaAttrGenerator(File file, String SDDName, Map<String, String> codeMap) {
		super(file);
		this.codeMap = codeMap;
		this.SDDName = SDDName;
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line =  "";
			while((line = br.readLine()) != null){
				String str[] = line.split(",", -1);
				List<String> tmp = new ArrayList<String>();
				tmp.add(str[2]);
				tmp.add(str[5]);
				hasEntityMap.put(str[0], tmp);
				System.out.println(str[0] + " *** " + tmp);
			}
			br.close();
		} catch (Exception e) {
			System.out.println("Error Reading File");			
		}
	}
	
	//Column	Attribute	attributeOf	Unit	Time	Entity	Role	Relation	inRelationTo	wasDerivedFrom	wasGeneratedBy	hasPosition	
	@Override
	void initMapping() {
		mapCol.clear();
		mapCol.put("Label", "Column");
		mapCol.put("AttributeType", "Attribute");
		mapCol.put("AttributeOf", "attributeOf");
		mapCol.put("Unit", "Unit");
		mapCol.put("Time", "Time");
		mapCol.put("Entity", "Entity");
		mapCol.put("Role", "Role");
		mapCol.put("Relation", "Relation");
		mapCol.put("InRelationTo", "inRelationTo");
		mapCol.put("WasDerivedFrom", "wasDerivedFrom");       
		mapCol.put("WasGeneratedBy", "wasGeneratedBy");
	}

	private String getLabel(CSVRecord rec) {
		return getValueByColumnName(rec, mapCol.get("Label"));
	}

	private String getAttribute(CSVRecord rec) {
		return getValueByColumnName(rec, mapCol.get("AttributeType"));
	}

	private String getAttributeOf(CSVRecord rec) {
		if (getValueByColumnName(rec, mapCol.get("AttributeOf").trim()).equals("")) {
			return "";
		}
		//System.out.println("DASchemaAttrGenerator: getAttributeOf() = " + SDDName + "-" + rec.get(mapCol.get("AttributeOf")).replace("??", ""));
		return kbPrefix + "DASO-" + SDDName + "-" + getValueByColumnName(rec, mapCol.get("AttributeOf")).replace("??", "");
	}

	private String getUnit(CSVRecord rec) {
		String original = getValueByColumnName(rec, mapCol.get("Unit"));
		if (ValueCellProcessing.isValidURI(original)) {
			return original;
		} else if (codeMap.containsKey(original)) {
			return codeMap.get(original);
		}
		
		return "obo:UO_0000186";
	}

	private String getTime(CSVRecord rec) {
		if (getValueByColumnName(rec, mapCol.get("Time").trim()).equals("")) {
			return "";
		}
		//System.out.println("DASchemaAttrGenerator: getTime() = " + SDDName + "-" + rec.get(mapCol.get("Time")).trim().replace(" ","").replace("_","-").replace("??", ""));
		return kbPrefix + "DASE-" + SDDName + "-" + getValueByColumnName(rec, mapCol.get("Time")).trim().replace(" ","").replace("_","-").replace("??", "");
	}

	private String getEntity(CSVRecord rec) {
		if (getValueByColumnName(rec, mapCol.get("AttributeOf")).equals("")) {
			return "chear:unknownEntity";
		} else {
			if (codeMap.containsKey(hasEntityMap.get(getValueByColumnName(rec, mapCol.get("AttributeOf"))))) {
				System.out.println("codeMap: " + codeMap.get(hasEntityMap.get(getValueByColumnName(rec, mapCol.get("AttributeOf")))));
				return codeMap.get(hasEntityMap.get(getValueByColumnName(rec, mapCol.get("AttributeOf"))));
			} else {
//				System.out.println(hasEntityMap.get(getValueByColumnName(rec, mapCol.get("AttributeOf"))).get(1));
				if (hasEntityMap.containsKey(getValueByColumnName(rec, mapCol.get("AttributeOf")))){
					return hasEntityMap.get(getValueByColumnName(rec, mapCol.get("AttributeOf"))).get(1);
				} else {
					return "chear:unknownEntity";
				}
			}
		}
	}

	private String getRole(CSVRecord rec) {
		return getValueByColumnName(rec, mapCol.get("Role"));
	}

	private String getRelation(CSVRecord rec) {
		return getValueByColumnName(rec, mapCol.get("Relation"));
	}

	private String getInRelationTo(CSVRecord rec) {
		return getValueByColumnName(rec, mapCol.get("InRelationTo"));
	}

	private String getWasDerivedFrom(CSVRecord rec) {
		//replace & with , for excel approach
		return getValueByColumnName(rec, mapCol.get("WasDerivedFrom"));
	}

	private String getWasGeneratedBy(CSVRecord rec) {
		return getValueByColumnName(rec, mapCol.get("WasGeneratedBy"));
	}

	private Boolean checkVirtual(CSVRecord rec) {
		if (getLabel(rec).contains("??")){
			return true;
		} else {
			return false;
		}
	}

	@Override
	public List< Map<String, Object> > createRows() throws Exception {
		rows.clear();
		int row_number = 0;
		for (CSVRecord record : records) {
			if (getAttribute(record)  == null || getAttribute(record).equals("")){
				continue;
			} else {
				rows.add(createRow(record, ++row_number));
			}
		}

		return rows;
	}
	
	public List<String> createUris() throws Exception {
		List<String> result = new ArrayList<String>();
		for (CSVRecord record : records) {
			if (getAttribute(record)  == null || getAttribute(record).equals("")){
				continue;
			} else {
				result.add(kbPrefix + "DASA-" + SDDName + "-" + getLabel(record).trim().replace(" ", "").replace("_","-").replace("??", ""));
			}
		}
		return result;
	}
	

	//Column	Attribute	attributeOf	Unit	Time	Entity	Role	Relation	inRelationTo	wasDerivedFrom	wasGeneratedBy	hasPosition   
	@Override
	Map<String, Object> createRow(CSVRecord rec, int row_number) throws Exception {
		Map<String, Object> row = new HashMap<String, Object>();
		row.put("hasURI", kbPrefix + "DASA-" + SDDName + "-" + getLabel(rec).trim().replace(" ", "").replace("_","-").replace("??", ""));
		row.put("a", "hasco:DASchemaAttribute");
		row.put("rdfs:label", getLabel(rec));
		row.put("rdfs:comment", getLabel(rec));
		row.put("hasco:partOfSchema", kbPrefix + "DAS-" + SDDName);
		row.put("hasco:hasEntity", getEntity(rec));
		System.out.println("hasco:hasEntity: " + getEntity(rec));
		row.put("hasco:hasAttribute", getAttribute(rec));
		row.put("hasco:hasUnit", getUnit(rec));
		row.put("hasco:hasEvent", getTime(rec));
		row.put("hasco:hasSource", "");
		row.put("hasco:isAttributeOf", getAttributeOf(rec));
		row.put("hasco:isVirtual", checkVirtual(rec).toString());
		row.put("hasco:isPIConfirmed", "false");

		return row;
	}
}

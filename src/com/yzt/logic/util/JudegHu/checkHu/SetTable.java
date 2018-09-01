package com.yzt.logic.util.JudegHu.checkHu;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import com.yzt.logic.util.Cnst;

public class SetTable {
	private HashMap<Integer, Boolean> m_tbl = new HashMap<Integer, Boolean>();

	public boolean check(Integer number) {
		return m_tbl.containsKey(number);
	}

	public void add(int key) {
		if (m_tbl.containsKey(key))
			return;

		m_tbl.put(key, true);
	}

	public void dump(String name) {
	}

	public void load(String path) {
		BufferedReader br = null;
		InputStreamReader inputStreamRreader = null;
		try {
//			br = new BufferedReader(new FileReader(Cnst.rootPath+path));
			inputStreamRreader = new InputStreamReader(SetTable.class.getClassLoader().getResourceAsStream(Cnst.rootPath+path));
			br = new BufferedReader(inputStreamRreader);
			String line = "";
			// int num=0;
			while ((line = br.readLine()) != null) {
				// System.out.println("line:" + line);
				m_tbl.put(Integer.parseInt(line), true);
				// num++;
			}
			br.close();
			// System.out.printf("load %s: num=%d\n", path, num);
			// if(m_tbl.containsKey(200000000)){
			// System.out.println("contains 200000000");
			// }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				if (inputStreamRreader!=null) {
					inputStreamRreader.close();
				}
				if (br!=null) {
					br.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}

package com.parc.troy;

import sml.Identifier;
import sml.WMElement;

public class SoarHelper {

	public static void deleteAllChildren(Identifier id){
		int index = 0;
		while (id.GetNumberChildren() > 0){
			WMElement child = id.GetChild(index);
			if (child != null) {
					child.DestroyWME();
				}
		}
	}
}

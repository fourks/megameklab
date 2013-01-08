/*
 * MegaMekLab - Copyright (C) 2011
 * 
 * Original author - jtighe (torren@users.sourceforge.net)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 */

package megameklab.com.util;

import java.awt.Component;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import megamek.common.AmmoType;
import megamek.common.Entity;
import megamek.common.EquipmentType;
import megamek.common.MiscType;
import megamek.common.TechConstants;
import megamek.common.WeaponType;

/**
 * this model was not being used by anything, so I totally redid so that it 
 * can be used as the model for the equipment tab. It will be a sortable, filterable
 * table of equipment, similar to the tables in MHQ
 * @author Jay lawson
 */
public class EquipmentTableModel extends AbstractTableModel {

	private static final long serialVersionUID = -5207167419079014157L;

	private final static int COL_NAME      = 0;
	private final static int COL_DAMAGE    = 1;
    private final static int COL_HEAT      = 2;
    private final static int COL_RANGE     = 3;
    private final static int COL_TON       = 4;
    private final static int COL_CRIT      = 5;
    private final static int COL_BV        = 6;
    private final static int COL_TECH      = 7;
    private final static int COL_TRATING   = 8;
    private final static int COL_DATES     = 9;
    private final static int COL_COST      = 10;
    public final static int N_COL          = 11;

    private ArrayList<EquipmentType> data = new ArrayList<EquipmentType>();
    private Entity entity = null;
    
    public EquipmentTableModel(Entity e) {
    	entity = e;
    }
    
    public int getRowCount() {
        return data.size();
    }

    public int getColumnCount() {
        return N_COL;
    }

    @Override
    public String getColumnName(int column) {
        switch(column) {
            case COL_NAME:
                return "Name";
            case COL_DAMAGE:
                return "Damage";
            case COL_HEAT:
                return "Heat";
            case COL_RANGE:
                return "Range";
            case COL_TON:
                return "Tonnage";
            case COL_CRIT:
                return "Crits";
            case COL_TECH:
                return "Tech";
            case COL_TRATING:
                return "Rating";
            case COL_COST:
                return "Cost";
            case COL_BV:
                return "BV";
            case COL_DATES:
                return "Dates";
            default:
                return "?";
        }
    }

    public int getColumnWidth(int c) {
        switch(c) {
        case COL_NAME:
            return 100;
        case COL_DATES:
            return 100;
        case COL_RANGE:
            return 30;
        case COL_TRATING:
        case COL_COST:
        	return 20;
        default:
            return 10;
        }
    }

    public int getAlignment(int col) {
        switch(col) {
        case COL_NAME:
        //case COL_DATES:
        	return SwingConstants.LEFT;
        case COL_TRATING:
        case COL_TECH:
        case COL_RANGE:
        	return SwingConstants.CENTER;
        default:
        	return SwingConstants.CENTER;
        }
    }

    public String getTooltip(int row, int col) {
    	EquipmentType type = data.get(row);
    	switch(col) {
    	case COL_DATES:
            return "Intro/Extinct/Re-Intro";
    	case COL_RANGE:
            return "Min/Short/Medium/Long";
        default:
        	return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    public EquipmentType getType(int i) {
    	if( i >= data.size()) {
    		return null;
    	}
        return data.get(i);
    }

    //fill table with values
    public void setData(ArrayList<EquipmentType> equip) {
        data = equip;
        fireTableDataChanged();
    }

    public Object getValueAt(int row, int col) {
    	EquipmentType type;
    	WeaponType wtype = null;
    	AmmoType atype = null;
    	MiscType mtype = null;
    	if(data.isEmpty()) {
    		return "";
    	} else {
    		type = data.get(row);
    	}
    	if(type instanceof WeaponType) {
    		wtype = (WeaponType)type;
    	}
    	if(type instanceof AmmoType) {
    		atype = (AmmoType)type;
    	}
    	if(type instanceof MiscType) {
    		mtype = (MiscType)type;
    	}
    	DecimalFormat formatter = new DecimalFormat();
    	
        if(col == COL_NAME) {
            return type.getName();
        }
        if(col == COL_DAMAGE) {
        	if(null != wtype) {
        		return getDamageString(wtype);
        	} else {
        		return "-";
        	}
        }
        if(col == COL_HEAT) {
        	if(null != wtype) {
        		return wtype.getHeat();
        	} else {
        		return "-";
        	}
        }
        if(col == COL_RANGE) {
        	if(null != wtype) {
        		int minRange = wtype.getMinimumRange();
        		if(minRange < 0) {
        			minRange = 0;
        		}
        		return minRange + "/" + wtype.getShortRange() + "/" + wtype.getMediumRange() + "/" + wtype.getLongRange();
        	} else {
        		return "-";
        	}
        }
        if(col == COL_TON) {
            return type.getTonnage(entity);
        }
        if(col == COL_CRIT) {
            return type.getCriticals(entity);
        }
        if(col == COL_TRATING) {
            return type.getFullRatingName();
        }
        if(col == COL_COST) {
            return type.getCost(entity, false, Entity.LOC_NONE);
        }
        if(col == COL_BV) {
            return type.getBV(entity);
        }
        if(col == COL_DATES) {
            return EquipmentType.getEquipDateAsString(type.getIntroductionDate()) + "/" + EquipmentType.getEquipDateAsString(type.getExtinctionDate()) + "/" + EquipmentType.getEquipDateAsString(type.getReintruductionDate());
        }
        if(col == COL_TECH) {
        	return TechConstants.isClan(type.getTechLevel()) ? "Clan" : "IS";
        }
        return "?";
    }
    
    private static String getDamageString(WeaponType wtype) {
    	if(wtype.getDamage() == WeaponType.DAMAGE_VARIABLE) {
    		return wtype.getDamage(WeaponType.RANGE_SHORT) + "/" + wtype.getDamage(WeaponType.RANGE_MED) + "/" + wtype.getDamage(WeaponType.RANGE_LONG);
    	}
    	else if(wtype.getDamage() == WeaponType.DAMAGE_BY_CLUSTERTABLE) {
    		return "Cluster";
    	}
    	else if(wtype.getDamage() < 0) {
    		return "Special";
    	}
    	else {
    		return Integer.toString(wtype.getDamage());
    	}
    }

    public EquipmentTableModel.Renderer getRenderer() {
		return new EquipmentTableModel.Renderer();
	}

	public class Renderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 9054581142945717303L;

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected,
					hasFocus, row, column);
			setOpaque(true);
			//setFont(new Font("Arial", Font.PLAIN, 12));
			int actualCol = table.convertColumnIndexToModel(column);
			int actualRow = table.convertRowIndexToModel(row);
			setHorizontalAlignment(getAlignment(actualCol));
			setToolTipText(getTooltip(actualRow, actualCol));
			return this;
		}
	}

}
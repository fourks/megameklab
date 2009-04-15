/*
 * MegaMekLab - Copyright (C) 2008
 *
 * Original author - jtighe (torren@users.sourceforge.net)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 */

package megameklab.com.util;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.TransferHandler;

import megamek.common.Entity;
import megamek.common.EquipmentType;
import megamek.common.LocationFullException;
import megamek.common.Mech;
import megamek.common.MiscType;
import megamek.common.Mounted;
import megamek.common.QuadMech;

public class CriticalTransferHandler extends TransferHandler {

    /**
     *
     */
    private static final long serialVersionUID = -5215375829853683877L;
    private Mech unit;
    private int location;
    private RefreshListener refresh;

    public CriticalTransferHandler(Mech unit, RefreshListener refresh) {
        this.unit = unit;
        this.refresh = refresh;
    }

    @Override
    public boolean importData(TransferSupport info) {
        if (!info.isDrop()) {
            return false;
        }

        if (info.getComponent() instanceof DropTargetCriticalList) {
            DropTargetCriticalList list = (DropTargetCriticalList) info.getComponent();
            location = Integer.parseInt(list.getName());
            Transferable t = info.getTransferable();
            try {
                String mountName = (String) t.getTransferData(DataFlavor.stringFlavor);

                Mounted eq = UnitUtil.getMounted(unit, mountName);

                if (eq.getType() instanceof MiscType && (eq.getType().hasFlag(MiscType.F_CLUB) || eq.getType().hasFlag(MiscType.F_HAND_WEAPON))) {
                    if (unit instanceof QuadMech) {
                        JOptionPane.showMessageDialog(null, "Quads Cannot use Physcial Weapons!", "Not Physicals For Quads", JOptionPane.INFORMATION_MESSAGE);
                        return false;
                    }

                    if (location != Mech.LOC_RARM && location != Mech.LOC_LARM) {
                        JOptionPane.showMessageDialog(null, "Physical Weapons can only go in the arms!", "Bad Location", JOptionPane.INFORMATION_MESSAGE);
                        return false;
                    }
                }

                int totalCrits = UnitUtil.getCritsUsed(unit, eq.getType());
                if ((eq.getType().isSpreadable() || eq.isSplitable()) && totalCrits > 1) {
                    int critsUsed = 0;
                    int primaryLocation = location;
                    int nextLocation = unit.getTransferLocation(location);
                    int emptyCrits = unit.getEmptyCriticals(location);

                    if (eq.getType().getCriticals(unit) > unit.getEmptyCriticals(location)) {
                        if (location == Mech.LOC_RT) {
                            String[] locations = { "Center Torso", "Right Leg", "Right Arm" };
                            JComboBox combo = new JComboBox(locations);
                            JOptionPane jop = new JOptionPane(combo, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);

                            JDialog dlg = jop.createDialog("Select secondary location.");
                            combo.grabFocus();
                            combo.getEditor().selectAll();

                            dlg.setVisible(true);

                            int value = ((Integer) jop.getValue()).intValue();

                            if (value == JOptionPane.CANCEL_OPTION) {
                                return false;
                            }

                            if (combo.getSelectedIndex() == 1) {
                                nextLocation = Mech.LOC_RLEG;
                            } else if (combo.getSelectedIndex() == 2) {
                                nextLocation = Mech.LOC_RARM;
                            }

                        } else if (location == Mech.LOC_LT) {
                            String[] locations = { "Center Torso", "Left Leg", "Leg Arm" };
                            JComboBox combo = new JComboBox(locations);
                            JOptionPane jop = new JOptionPane(combo, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);

                            JDialog dlg = jop.createDialog("Select secondary location.");
                            combo.grabFocus();
                            combo.getEditor().selectAll();

                            dlg.setVisible(true);

                            int value = ((Integer) jop.getValue()).intValue();

                            if (value == JOptionPane.CANCEL_OPTION) {
                                return false;
                            }

                            if (combo.getSelectedIndex() == 1) {
                                nextLocation = Mech.LOC_LLEG;
                            } else if (combo.getSelectedIndex() == 2) {
                                nextLocation = Mech.LOC_LARM;
                            }

                        } else if (location == Mech.LOC_CT) {
                            String[] locations = { "Left Torso", "Right Torso" };
                            JComboBox combo = new JComboBox(locations);
                            JOptionPane jop = new JOptionPane(combo, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);

                            JDialog dlg = jop.createDialog(null, "Select secondary location.");
                            combo.grabFocus();
                            combo.getEditor().selectAll();

                            dlg.setVisible(true);

                            int value = ((Integer) jop.getValue()).intValue();

                            if (value == JOptionPane.CANCEL_OPTION) {
                                return false;
                            }

                            if (combo.getSelectedIndex() == 1) {
                                nextLocation = Mech.LOC_RT;
                            } else {
                                nextLocation = Mech.LOC_LT;
                            }
                        }
                    }
                    // No big splitables in the head!
                    if (emptyCrits < totalCrits && (nextLocation == Entity.LOC_DESTROYED || (unit.getEmptyCriticals(location) + unit.getEmptyCriticals(nextLocation) < totalCrits))) {
                        throw new LocationFullException(eq.getName() + " does not fit in " + unit.getLocationAbbr(location) + " on " + unit.getDisplayName());
                    }
                    for (; critsUsed < totalCrits; critsUsed++) {
                        unit.addEquipment(eq, location, false);
                        if (unit.getEmptyCriticals(location) == 0) {
                            location = nextLocation;
                            totalCrits -= critsUsed;
                            critsUsed = 0;
                        }
                    }
                    changeMountStatus(eq, primaryLocation, nextLocation, false);
                } else if (UnitUtil.getHighestContinuousNumberOfCrits(unit, location) >= totalCrits) {
                    unit.addEquipment(eq, location, false);
                    changeMountStatus(eq, location, false);
                } else {
                    throw new LocationFullException(eq.getName() + " does not fit in " + unit.getLocationAbbr(location) + " on " + unit.getDisplayName());
                }
            } catch (LocationFullException lfe) {
                JOptionPane.showMessageDialog(null, lfe.getMessage(), "Location Full", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return true;
        }
        return false;
    }

    @Override
    public boolean canImport(TransferSupport info) {
        // Check for String flavor
        if (!info.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            return false;
        }
        return true;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        JTable table = (JTable) c;
        EquipmentType eq = (EquipmentType) ((CriticalTableModel) table.getModel()).getValueAt(table.getSelectedRow(), CriticalTableModel.EQUIPMENT);
        return new StringSelection(eq.getInternalName());
    }

    @Override
    public int getSourceActions(JComponent c) {
        return TransferHandler.LINK;
    }

    private void changeMountStatus(Mounted eq, int location, boolean rear) {
        changeMountStatus(eq, location, -1, rear);
    }

    private void changeMountStatus(Mounted eq, int location, int secondaryLocation, boolean rear) {

        UnitUtil.changeMountStatus(unit, eq, location, secondaryLocation, rear);

        if (refresh != null) {
            refresh.refreshAll();
        }
    }

}
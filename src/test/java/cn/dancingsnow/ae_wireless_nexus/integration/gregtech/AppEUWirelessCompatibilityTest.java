package cn.dancingsnow.ae_wireless_nexus.integration.gregtech;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

import appeng.me.helpers.IGridProxyable;
import cn.dancingsnow.appeu.hatch.MTEHatchMEDynamo;
import cn.dancingsnow.appeu.hatch.MTEHatchMEDynamoMulti;
import cn.dancingsnow.appeu.hatch.MTEHatchMEDynamoTunnel;
import cn.dancingsnow.appeu.hatch.MTEHatchMEEnergy;
import cn.dancingsnow.appeu.hatch.MTEHatchMEEnergyMulti;
import cn.dancingsnow.appeu.hatch.MTEHatchMEEnergyTunnel;
import gregtech.api.interfaces.ITexture;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEBasicTank;
import gregtech.api.metatileentity.implementations.MTEHatchEnergy;

class AppEUWirelessCompatibilityTest {

    @Test
    void allAppEUMeEnergyHatchFamiliesAreWirelessEndpoints() {
        Class<?>[] hatchTypes = { MTEHatchMEEnergy.class, MTEHatchMEDynamo.class, MTEHatchMEEnergyMulti.class,
            MTEHatchMEDynamoMulti.class, MTEHatchMEEnergyTunnel.class, MTEHatchMEDynamoTunnel.class };

        for (Class<?> hatchType : hatchTypes) {
            assertTrue(
                GTWirelessEndpoint.isEligibleMetaTileEntityType(hatchType),
                () -> hatchType.getName() + " must satisfy the wireless hatch contract");
        }
    }

    @Test
    void ordinaryEnergyHatchesAndNonHatchesStayExcluded() {
        assertFalse(GTWirelessEndpoint.isEligibleMetaTileEntityType(MTEHatchEnergy.class));
        assertFalse(GTWirelessEndpoint.isEligibleMetaTileEntityType(IGridProxyable.class));
        assertFalse(GTWirelessEndpoint.isEligibleMetaTileEntityType(null));
    }

    @Test
    void zeroSlotAppEUHatchesInheritTheGenericTankUi() throws NoSuchMethodException {
        MetaTileEntity[] hatches = { new MTEHatchMEEnergy("test.energy", 0, new String[0], new ITexture[0][0][0]),
            new MTEHatchMEDynamo("test.dynamo", 0, 2, new String[0], new ITexture[0][0][0]),
            new MTEHatchMEEnergyMulti("test.energy.multi", 0, 4, new String[0], new ITexture[0][0][0]),
            new MTEHatchMEDynamoMulti("test.dynamo.multi", 0, 4, new String[0], new ITexture[0][0][0]) };

        for (MetaTileEntity hatch : hatches) {
            assertEquals(0, hatch.getSizeInventory());
            assertEquals(
                MTEBasicTank.class,
                hatch.getClass()
                    .getMethod("buildUI", PosGuiData.class, PanelSyncManager.class, UISettings.class)
                    .getDeclaringClass());
        }
    }

    @Test
    void laserHatchesKeepTheirDedicatedUi() throws NoSuchMethodException {
        Class<?>[] hatchTypes = { MTEHatchMEEnergyTunnel.class, MTEHatchMEDynamoTunnel.class };

        for (Class<?> hatchType : hatchTypes) {
            assertFalse(
                MTEBasicTank.class.equals(
                    hatchType.getMethod("buildUI", PosGuiData.class, PanelSyncManager.class, UISettings.class)
                        .getDeclaringClass()));
        }
    }
}

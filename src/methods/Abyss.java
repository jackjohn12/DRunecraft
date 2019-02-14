package methods;

import org.rspeer.runetek.api.Varps;
import org.rspeer.runetek.api.commons.BankLocation;
import org.rspeer.runetek.api.movement.position.Position;
import task_structure.TreeScript;
import task_structure.TreeTask;
import tasks.*;
import utils.AbyssObstacles;

public class Abyss extends CraftMethod {
    public Abyss(final TreeScript handler) {
        super(handler, CraftMethods.ABYSS.getId());
        final int miniquestCompleted = Varps.get(491);  //1073799168?
        handler.addNotedPosition("edgeville", new Position(3088, 3502));
        handler.addNotedPosition("mage", new Position(3107, 3557));
        handler.addNotedPosition("center abyss", new Position(3037, 4830));
        handler.addNotedPosition("outer ffa", new Position(3352, 3164));
        handler.addNotedPosition("inner ffa", new Position(3327, 4751));
        handler.addNotedPosition("clan wars", new Position(3369, 3170));
        handler.addNotedPosition("inside altar", new Position(2400, 4837));
        for (final AbyssObstacles obstacle : AbyssObstacles.getMiningLoadout())
            handler.addNotedSetting(obstacle.toString());
        /**
         * auto adding all pouches, should be more selective in GUI
         */
        handler.addNotedSetting("Small pouch");
        handler.addNotedSetting("Medium pouch");
        handler.addNotedSetting("Large pouch");
        handler.addNotedSetting("Giant pouch");
        final TreeTask head = new HasRunes("Nature");
        TreeTask second = head.setLeft(new HasPureEssence());
        TreeTask third = second.setLeft(new IsInAltar(handler));
        TreeTask fourth = third.setLeft(new IsBankOpen());
        TreeTask fifth = fourth.setLeft(new IsNearBank());
        TreeTask sixth = fifth.setLeft(new IsNearClanWars(handler));
        sixth.setLeft(new TeleportToClanWars(handler));
        sixth.setRight(new WalkToBank(BankLocation.CLAN_WARS));
        fifth.setRight(new OpenBank());
        fifth = fourth.setRight(new ArePouchesInInventory(handler));
        fifth.setLeft(new WithdrawPouches(handler));
        sixth = fifth.setRight(new IsRingEquipped());
        TreeTask seventh = sixth.setLeft(new IsRingInInventory());
        seventh.setLeft(new WithdrawRing());
        seventh.setRight(new EquipRing());
        seventh = sixth.setRight(new IsGloryEquipped());
        TreeTask eighth = seventh.setLeft(new IsGloryInInventory());
        eighth.setLeft(new WithdrawGlory());
        eighth.setRight(new EquipGlory());
        eighth = seventh.setRight(new IsBadGloryInInventory());
        eighth.setLeft(new WithdrawPureEssence());
        eighth.setRight(new DepositBadGlory());
        fourth = third.setRight(new DoPouchesNeedFilling(handler));
        fourth.setLeft(new EmptyPouches(handler));
        fourth.setRight(new TeleportToClanWars(handler));
        third = second.setRight(new IsInAltar(handler));
        fourth = third.setLeft(new IsBankOpen());
        fifth = fourth.setLeft(new DoPouchesNeedFilling(handler));
        sixth = fifth.setLeft(new IsEnoughEssenceInInventory());
        sixth.setLeft(new OpenBank());
        seventh = sixth.setRight(new IsInAbyss(handler));
        eighth = seventh.setLeft(new IsNearEdgeville(handler));
        TreeTask ninth = eighth.setLeft(new IsInFFA(handler));
        TreeTask tenth = ninth.setLeft(new IsNearClanWars(handler));
        tenth.setLeft(new TeleportToClanWars(handler));
        TreeTask eleventh = tenth.setRight(new IsBeingHunted(handler));
        eleventh.setLeft(new UseClanWarsPortal(handler));
        eleventh.setRight(new HopToFreshWorld(handler));
        ninth.setRight(new TeleportToEdgeville(handler));
        ninth = eighth.setRight(new IsNearMage());
        tenth = ninth.setLeft(new IsBeingHunted(handler));
        tenth.setLeft(new WalkToMage(handler));
        tenth.setRight(new TeleportToClanWars(handler));
        ninth.setRight(new TeleportToAbyss(handler));
        eighth = seventh.setRight(new IsInInnerAbyss());
        ninth = eighth.setLeft(new IsHealthBelowThreshold());
        ninth.setLeft(new TraverseObstacle(handler));
        ninth.setRight(new TeleportToClanWars(handler));
        ninth = eighth.setRight(new DoPouchesNeedRepairing(handler));
        ninth.setLeft(new EnterRift(handler));
        ninth.setRight(new RepairPouches());
        fifth.setRight(new FillPouches(handler));
        fifth = fourth.setRight(new IsEnoughEssenceInInventory());
        sixth = fifth.setLeft(new IsBadGloryInInventory());
        sixth.setLeft(new WithdrawPureEssence());
        sixth.setRight(new DepositBadGlory());
        fifth.setRight(new CloseBank());
        third.setRight(new CraftRunes(handler));
        second = head.setRight(new IsInAltar(handler));
        third = second.setLeft(new IsBankOpen());
        fourth = third.setLeft(new IsNearBank());
        fifth = fourth.setLeft(new IsNearClanWars(handler));
        fifth.setLeft(new TeleportToClanWars(handler));
        fifth.setRight(new WalkToBank(BankLocation.CLAN_WARS));
        fourth.setRight(new OpenBank());
        third.setRight(new DepositRunes());
        third = second.setRight(new HasPureEssence());
        fourth = third.setLeft(new AreAllPouchesEmpty(handler));
        fourth.setLeft(new EmptyPouches(handler));
        fourth.setRight(new TeleportToClanWars(handler));
        third.setRight(new CraftRunes(handler));
        setHead(head);
    }
}

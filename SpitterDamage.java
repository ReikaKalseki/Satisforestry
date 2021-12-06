package Reika.Satisforestry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.Potion;

import Reika.DragonAPI.Instantiable.CustomStringDamageSource;
import Reika.DragonAPI.Libraries.IO.ReikaPacketHelper;
import Reika.Satisforestry.SFPacketHandler.SFPackets;
import Reika.Satisforestry.Entity.EntitySpitter;
import Reika.Satisforestry.Entity.EntitySpitterFireball;


public final class SpitterDamage extends CustomStringDamageSource {

	private static final SpitterDamage spitterBlast = (SpitterDamage)new SpitterDamage().setExplosion();
	private static final SpitterDamage spitterBall = (SpitterDamage)new SpitterDamage().setFireDamage().setProjectile();
	private static final SpitterDamage spitterBallFallback = (SpitterDamage)new SpitterDamage().setProjectile();

	private EntitySpitter spitter;
	private EntitySpitterFireball fireball;

	public SpitterDamage() {
		super("was blown away by a ");
	}

	@Override
	protected String getMessage() {
		return spitter == null || spitter.getSpitterType() == null ? "[ERROR]" : super.getMessage()+spitter.getSpitterType().getName();
	}

	@Override
	public Entity getSourceOfDamage() {
		return spitter;
	}

	@Override
	public Entity getEntity() {
		return fireball == null ? spitter : fireball;
	}

	@Override
	public boolean isDifficultyScaled() {
		return true;
	}

	public static void doDamage(EntitySpitter es, EntitySpitterFireball f, EntityLivingBase tgt, float amt) {
		boolean resist = tgt.isImmuneToFire() || tgt.isPotionActive(Potion.fireResistance);
		boolean spitter = tgt instanceof EntitySpitter;
		SpitterDamage dmg = f != null ? (resist ? spitterBallFallback : spitterBall) : spitterBlast;
		if (spitter || dmg.isFireDamage()) {
			if (resist) {
				amt *= 0.5;
				if (!spitter)
					tgt.setFire(1);
			}
			else {
				tgt.setFire(es.getSpitterType().burnDuration);
				tgt.hurtResistantTime = Math.min(tgt.hurtResistantTime, 5);
			}
		}
		if (spitter && ((EntitySpitter)tgt).getSpitterType().isAlpha() && !es.getSpitterType().isAlpha())
			amt *= 0.75;
		if (es.isPotionActive(Potion.damageBoost)) {
			amt *= 1+0.333*(es.getActivePotionEffect(Potion.damageBoost).getAmplifier()+1);
		}
		if (es.riddenByEntity instanceof EntityPlayer) {
			EntityPlayer ep = (EntityPlayer)es.riddenByEntity;
			if (ep.isPotionActive(Potion.damageBoost)) {
				amt *= 1+0.1667*(ep.getActivePotionEffect(Potion.damageBoost).getAmplifier()+1);
			}
			int slug = SFAux.getSlugHelmetTier(ep);
			if (slug > 0) {
				amt *= 1+Math.pow(2, slug)/8D;
			}
		}
		tgt.attackEntityFrom(dmg, amt);
		if (tgt instanceof EntityPlayerMP)
			ReikaPacketHelper.sendDataPacket(Satisforestry.packetChannel, SFPackets.MOBDAMAGE.ordinal(), (EntityPlayerMP)tgt, (f != null ? f : es).getEntityId());
	}

}

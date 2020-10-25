package chronosacaria.mcdw.bases;

import chronosacaria.mcdw.Mcdw;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class McdwDoubleAxe extends AxeItem {
    public McdwDoubleAxe(ToolMaterial material, float attackDamage, float attackSpeed, String id){
        super(material, attackDamage, attackSpeed, new Settings().group(Mcdw.WEAPONS));
        Registry.register(Registry.ITEM, new Identifier(Mcdw.MOD_ID, id), this);
    }
}
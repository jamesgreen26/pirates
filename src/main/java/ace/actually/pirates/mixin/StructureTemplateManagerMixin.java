package ace.actually.pirates.mixin;

import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import java.util.Map;
import java.util.Optional;

/*
*    VS Crumbles compatibility :)
*/


@Mixin(value = StructureTemplateManager.class)
public abstract class StructureTemplateManagerMixin {

    @Final
    @Shadow
    private Map<Identifier, Optional<StructureTemplate>> templates;

    @Shadow
    protected abstract Optional<StructureTemplate> loadTemplate(Identifier identifier);

    /**
     * @author
     * G_Mungus
     * @reason
     * Easier than inject
     */
    @Overwrite
    public Optional<StructureTemplate> getTemplate(Identifier id) {

        Optional<StructureTemplate> template = this.templates.computeIfAbsent(id, this::loadTemplate);

        if (template.isPresent() && id.getNamespace().equals("pirates") && id.getPath().startsWith("ship/")) {
            template.get().setAuthor("pirate-ship");
        }

        return template;
    }



}

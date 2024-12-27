package ace.actually.pirates.mixin;

import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Optional;

@Mixin(value = StructureTemplateManager.class)
public abstract class StructureTemplateManagerMixin {

    @Final
    @Shadow
    private Map<Identifier, Optional<StructureTemplate>> templates;

    @Shadow
    protected abstract Optional<StructureTemplate> loadTemplate(Identifier identifier);

    @Inject(method = "getTemplate", at = @At("HEAD"), cancellable = true)
    public void getTemplateMixin(Identifier id, CallbackInfoReturnable<Optional<StructureTemplate>> cir) {

        Optional<StructureTemplate> template = this.templates.computeIfAbsent(id, this::loadTemplate);

        if (template.isPresent() && !template.get().getAuthor().equals("dirty") && id.getNamespace().equals("pirates") && id.getPath().startsWith("ship/")) {
            template.get().setAuthor("pirate-ship");
        }

        cir.setReturnValue(template);
    }



}

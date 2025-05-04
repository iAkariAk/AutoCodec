package io.github.iakakariak.autocodec.example;

import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;

import java.util.Optional;

@Mod(ExampleMain.ID)
public class ExampleMain {
    public static final String ID = "example";
    private static final Logger LOGGER = LogUtils.getLogger();

    public ExampleMain(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::registerPayloads);

    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    public void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playBidirectional(CialloPacket.TYPE, CialloCodes.STREAM_CODEC, (packet, context) -> {
            context.enqueueWork(() -> {
                LOGGER.info("CialloPacket～(∠・ω< )⌒☆ Player: {}, whose profile: {}, whose main hand: {}", packet.name(), packet.profile(), packet.hand());
            });
        });
    }

    @EventBusSubscriber(modid = ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClick(PlayerInteractEvent.LeftClickEmpty event) {
            var player = event.getEntity();
            var hand = player.getMainHandItem();
            PacketDistributor.sendToServer(new CialloPacket(
                    player.getDisplayName().getString(),
                    hand.isEmpty() ? Optional.empty() : Optional.of(hand), // Codecing an empty ItemStack is illegal.
                    player.getGameProfile())
            );
            LOGGER.info("🤝 with server");
        }
    }
}

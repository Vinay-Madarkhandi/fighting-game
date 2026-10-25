package com.vinay.minimilitia.weapons

enum class WeaponCategory {
    HANDGUN,
    SMG,
    RIFLE,
    SHOTGUN,
    SNIPER,
    HEAVY_SPECIAL,
    THROWABLE,
    MELEE
}

enum class ProjectileType {
    BULLET,
    PELLET,
    SNIPER_ROUND,
    ROCKET,
    GRENADE_SHELL,
    SAW_BLADE,
    LASER_BEAM,
    FLAME_PARTICLE,
    FRAG_GRENADE,
    GAS_GRENADE,
    PROXY_MINE,
    EMP_GRENADE
}

data class WeaponConfig(
    val id: String,
    val name: String,
    val category: WeaponCategory,
    val projectileType: ProjectileType,
    val damage: Float,
    val fireRate: Float, // Seconds between shots
    val bulletSpeed: Float, // m/s
    val clipSize: Int,
    val maxReserveAmmo: Int,
    val reloadTime: Float, // Seconds
    val recoilAngle: Float, // Degrees
    val spreadAngle: Float, // Degrees
    val pelletsPerShot: Int = 1,
    val burstCount: Int = 1,
    val burstInterval: Float = 0.08f,
    val maxZoom: Float = 1.0f,
    val canDualWield: Boolean = false,
    val soundEffect: String,
    val reloadSound: String = "reload.wav",
    val description: String
)

object WeaponDefinitions {
    val ALL_WEAPONS: Map<String, WeaponConfig> = listOf(
        // Handguns
        WeaponConfig(
            id = "deagle",
            name = "Desert Eagle",
            category = WeaponCategory.HANDGUN,
            projectileType = ProjectileType.BULLET,
            damage = 34f,
            fireRate = 0.22f,
            bulletSpeed = 38f,
            clipSize = 7,
            maxReserveAmmo = 35,
            reloadTime = 1.4f,
            recoilAngle = 3.5f,
            spreadAngle = 2.0f,
            canDualWield = true,
            soundEffect = "deagle.wav",
            description = "High power semi-automatic handgun."
        ),
        WeaponConfig(
            id = "magnum",
            name = ".44 Magnum",
            category = WeaponCategory.HANDGUN,
            projectileType = ProjectileType.BULLET,
            damage = 56f,
            fireRate = 0.38f,
            bulletSpeed = 42f,
            clipSize = 6,
            maxReserveAmmo = 30,
            reloadTime = 1.8f,
            recoilAngle = 6.0f,
            spreadAngle = 1.5f,
            maxZoom = 1.1f,
            canDualWield = true,
            soundEffect = "magnum.wav",
            description = "Heavy revolver delivering devastating stopping power."
        ),
        WeaponConfig(
            id = "huntingpistol",
            name = "Hunting Pistol",
            category = WeaponCategory.HANDGUN,
            projectileType = ProjectileType.BULLET,
            damage = 48f,
            fireRate = 0.32f,
            bulletSpeed = 40f,
            clipSize = 8,
            maxReserveAmmo = 40,
            reloadTime = 1.5f,
            recoilAngle = 4.5f,
            spreadAngle = 1.8f,
            maxZoom = 1.15f,
            canDualWield = true,
            soundEffect = "deagle.wav",
            description = "Accurate long-barrel pistol for precision engagement."
        ),
        WeaponConfig(
            id = "tec9",
            name = "TEC-9",
            category = WeaponCategory.HANDGUN,
            projectileType = ProjectileType.BULLET,
            damage = 18f,
            fireRate = 0.10f,
            bulletSpeed = 32f,
            clipSize = 20,
            maxReserveAmmo = 80,
            reloadTime = 1.5f,
            recoilAngle = 4.0f,
            spreadAngle = 5.0f,
            canDualWield = true,
            soundEffect = "tec9.wav",
            description = "Rapid firing machine pistol effective in close combat."
        ),

        // SMGs
        WeaponConfig(
            id = "uzi",
            name = "Micro UZI",
            category = WeaponCategory.SMG,
            projectileType = ProjectileType.BULLET,
            damage = 16f,
            fireRate = 0.08f,
            bulletSpeed = 32f,
            clipSize = 25,
            maxReserveAmmo = 100,
            reloadTime = 1.3f,
            recoilAngle = 4.5f,
            spreadAngle = 6.0f,
            canDualWield = true,
            soundEffect = "uzi.wav",
            description = "Fastest firing submachine gun. Blistering burst damage."
        ),
        WeaponConfig(
            id = "mp5",
            name = "MP5",
            category = WeaponCategory.SMG,
            projectileType = ProjectileType.BULLET,
            damage = 22f,
            fireRate = 0.11f,
            bulletSpeed = 35f,
            clipSize = 30,
            maxReserveAmmo = 120,
            reloadTime = 1.5f,
            recoilAngle = 3.5f,
            spreadAngle = 3.5f,
            canDualWield = true,
            soundEffect = "mp5.wav",
            description = "Balanced submachine gun with high control and fire rate."
        ),

        // Assault Rifles
        WeaponConfig(
            id = "ak47",
            name = "AK-47",
            category = WeaponCategory.RIFLE,
            projectileType = ProjectileType.BULLET,
            damage = 28f,
            fireRate = 0.13f,
            bulletSpeed = 38f,
            clipSize = 30,
            maxReserveAmmo = 120,
            reloadTime = 1.8f,
            recoilAngle = 4.5f,
            spreadAngle = 3.0f,
            maxZoom = 1.2f,
            soundEffect = "ak47.wav",
            description = "Rugged, high-damage combat assault rifle."
        ),
        WeaponConfig(
            id = "m16",
            name = "M16",
            category = WeaponCategory.RIFLE,
            projectileType = ProjectileType.BULLET,
            damage = 26f,
            fireRate = 0.40f, // Burst recycle delay
            bulletSpeed = 42f,
            clipSize = 30,
            maxReserveAmmo = 120,
            reloadTime = 1.6f,
            recoilAngle = 2.5f,
            spreadAngle = 1.5f,
            burstCount = 3,
            burstInterval = 0.07f,
            maxZoom = 1.25f,
            soundEffect = "m16.wav",
            description = "3-round burst assault rifle for pinpoint accuracy."
        ),
        WeaponConfig(
            id = "tavor",
            name = "TAVOR TAR-21",
            category = WeaponCategory.RIFLE,
            projectileType = ProjectileType.BULLET,
            damage = 25f,
            fireRate = 0.11f,
            bulletSpeed = 40f,
            clipSize = 30,
            maxReserveAmmo = 120,
            reloadTime = 1.5f,
            recoilAngle = 3.5f,
            spreadAngle = 2.5f,
            maxZoom = 1.2f,
            soundEffect = "tavor.wav",
            description = "Bullpup assault rifle with compact handling and fast reload."
        ),
        WeaponConfig(
            id = "xm8",
            name = "XM8",
            category = WeaponCategory.RIFLE,
            projectileType = ProjectileType.BULLET,
            damage = 27f,
            fireRate = 0.12f,
            bulletSpeed = 41f,
            clipSize = 30,
            maxReserveAmmo = 120,
            reloadTime = 1.7f,
            recoilAngle = 3.0f,
            spreadAngle = 2.2f,
            maxZoom = 1.25f,
            soundEffect = "xm8.wav",
            description = "Modernized modular rifle with integrated scope optics."
        ),

        // Shotguns
        WeaponConfig(
            id = "shotgun",
            name = "Pump Shotgun",
            category = WeaponCategory.SHOTGUN,
            projectileType = ProjectileType.PELLET,
            damage = 14f, // 8 pellets = 112 point-blank damage
            fireRate = 0.75f,
            bulletSpeed = 30f,
            clipSize = 6,
            maxReserveAmmo = 36,
            reloadTime = 2.2f,
            recoilAngle = 9.0f,
            spreadAngle = 16.0f,
            pelletsPerShot = 8,
            soundEffect = "shotgun.wav",
            description = "Pump-action spread gun. Devastating at close range."
        ),
        WeaponConfig(
            id = "aa12",
            name = "AA-12 Auto Shotgun",
            category = WeaponCategory.SHOTGUN,
            projectileType = ProjectileType.PELLET,
            damage = 12f, // 6 pellets = 72 damage per shot
            fireRate = 0.26f,
            bulletSpeed = 28f,
            clipSize = 12,
            maxReserveAmmo = 48,
            reloadTime = 2.6f,
            recoilAngle = 7.0f,
            spreadAngle = 20.0f,
            pelletsPerShot = 6,
            soundEffect = "shotgun.wav",
            description = "Fully automatic assault shotgun for area clearing."
        ),

        // Snipers
        WeaponConfig(
            id = "m93ba",
            name = "M93BA Sniper Rifle",
            category = WeaponCategory.SNIPER,
            projectileType = ProjectileType.SNIPER_ROUND,
            damage = 95f,
            fireRate = 1.2f,
            bulletSpeed = 65f,
            clipSize = 5,
            maxReserveAmmo = 20,
            reloadTime = 2.4f,
            recoilAngle = 12.0f,
            spreadAngle = 0.2f,
            maxZoom = 2.2f,
            soundEffect = "m93ba.wav",
            description = "Heavy caliber sniper rifle with extreme zoom and lethal power."
        ),
        WeaponConfig(
            id = "m14",
            name = "M14 DMR",
            category = WeaponCategory.SNIPER,
            projectileType = ProjectileType.SNIPER_ROUND,
            damage = 46f,
            fireRate = 0.35f,
            bulletSpeed = 50f,
            clipSize = 10,
            maxReserveAmmo = 40,
            reloadTime = 1.9f,
            recoilAngle = 6.5f,
            spreadAngle = 0.8f,
            maxZoom = 1.6f,
            soundEffect = "m14.wav",
            description = "Designated marksman rifle combining rate of fire with range."
        ),

        // Heavy & Special
        WeaponConfig(
            id = "rocket",
            name = "SMAW Rocket Launcher",
            category = WeaponCategory.HEAVY_SPECIAL,
            projectileType = ProjectileType.ROCKET,
            damage = 140f, // Direct hit + 110 splash damage (3.5m radius)
            fireRate = 1.6f,
            bulletSpeed = 20f,
            clipSize = 1,
            maxReserveAmmo = 6,
            reloadTime = 2.5f,
            recoilAngle = 14.0f,
            spreadAngle = 1.0f,
            maxZoom = 1.2f,
            soundEffect = "rocket.wav",
            description = "Fires high-explosive anti-armor rockets with massive blast."
        ),
        WeaponConfig(
            id = "rg6",
            name = "RG-6 Grenade Launcher",
            category = WeaponCategory.HEAVY_SPECIAL,
            projectileType = ProjectileType.GRENADE_SHELL,
            damage = 95f,
            fireRate = 0.55f,
            bulletSpeed = 22f,
            clipSize = 6,
            maxReserveAmmo = 18,
            reloadTime = 2.8f,
            recoilAngle = 7.0f,
            spreadAngle = 3.0f,
            soundEffect = "rg6.wav",
            description = "Revolving cylinder grenade launcher firing bouncing explosive rounds."
        ),
        WeaponConfig(
            id = "sawgun",
            name = "Saw Blade Launcher",
            category = WeaponCategory.HEAVY_SPECIAL,
            projectileType = ProjectileType.SAW_BLADE,
            damage = 75f,
            fireRate = 0.5f,
            bulletSpeed = 24f,
            clipSize = 5,
            maxReserveAmmo = 15,
            reloadTime = 2.0f,
            recoilAngle = 5.0f,
            spreadAngle = 2.0f,
            soundEffect = "saw.wav",
            description = "Launches spinning serrated blades that ricochet off walls."
        ),
        WeaponConfig(
            id = "phasr",
            name = "PHASR Laser",
            category = WeaponCategory.HEAVY_SPECIAL,
            projectileType = ProjectileType.LASER_BEAM,
            damage = 12f, // 12 damage every 0.06s = 200 DPS
            fireRate = 0.06f,
            bulletSpeed = 90f,
            clipSize = 40,
            maxReserveAmmo = 160,
            reloadTime = 1.8f,
            recoilAngle = 0.8f,
            spreadAngle = 0.5f,
            maxZoom = 1.35f,
            soundEffect = "laser.wav",
            description = "Emits continuous coherent particle laser beam."
        ),
        WeaponConfig(
            id = "flame",
            name = "Flamethrower",
            category = WeaponCategory.HEAVY_SPECIAL,
            projectileType = ProjectileType.FLAME_PARTICLE,
            damage = 9f, // 180 DPS + burning damage
            fireRate = 0.05f,
            bulletSpeed = 16f,
            clipSize = 50,
            maxReserveAmmo = 200,
            reloadTime = 2.2f,
            recoilAngle = 1.5f,
            spreadAngle = 12.0f,
            soundEffect = "flame.wav",
            description = "Projects stream of ignited fuel burning through defenses."
        ),
        WeaponConfig(
            id = "machete",
            name = "Machete",
            category = WeaponCategory.MELEE,
            projectileType = ProjectileType.BULLET,
            damage = 80f,
            fireRate = 0.4f,
            bulletSpeed = 0f,
            clipSize = 1,
            maxReserveAmmo = 1,
            reloadTime = 0.1f,
            recoilAngle = 0f,
            spreadAngle = 0f,
            soundEffect = "melee.wav",
            description = "Heavy steel blade for immediate close-range execution."
        ),

        // Throwables
        WeaponConfig(
            id = "fragnade",
            name = "Frag Grenade",
            category = WeaponCategory.THROWABLE,
            projectileType = ProjectileType.FRAG_GRENADE,
            damage = 130f,
            fireRate = 1.0f,
            bulletSpeed = 16f,
            clipSize = 3,
            maxReserveAmmo = 6,
            reloadTime = 0.5f,
            recoilAngle = 0f,
            spreadAngle = 0f,
            soundEffect = "grenade1.wav",
            description = "Standard fragmentation explosive with 2.5s timed fuse."
        ),
        WeaponConfig(
            id = "gasnade",
            name = "Poison Gas Grenade",
            category = WeaponCategory.THROWABLE,
            projectileType = ProjectileType.GAS_GRENADE,
            damage = 25f, // Damage per second in cloud
            fireRate = 1.0f,
            bulletSpeed = 15f,
            clipSize = 3,
            maxReserveAmmo = 6,
            reloadTime = 0.5f,
            recoilAngle = 0f,
            spreadAngle = 0f,
            soundEffect = "gas.wav",
            description = "Emits suffocating toxic cloud that lingers for 8 seconds."
        ),
        WeaponConfig(
            id = "proxynade",
            name = "Proximity Mine",
            category = WeaponCategory.THROWABLE,
            projectileType = ProjectileType.PROXY_MINE,
            damage = 135f,
            fireRate = 1.0f,
            bulletSpeed = 14f,
            clipSize = 3,
            maxReserveAmmo = 6,
            reloadTime = 0.5f,
            recoilAngle = 0f,
            spreadAngle = 0f,
            soundEffect = "proxy.wav",
            description = "Sticky sensor mine detonating when enemies step close."
        ),
        WeaponConfig(
            id = "empnade",
            name = "EMP Grenade",
            category = WeaponCategory.THROWABLE,
            projectileType = ProjectileType.EMP_GRENADE,
            damage = 40f,
            fireRate = 1.0f,
            bulletSpeed = 16f,
            clipSize = 3,
            maxReserveAmmo = 6,
            reloadTime = 0.5f,
            recoilAngle = 0f,
            spreadAngle = 0f,
            soundEffect = "grenade2.wav",
            description = "Electromagnetic pulse disabling enemy jetpack boost for 5s."
        )
    ).associateBy { it.id }

    fun get(id: String): WeaponConfig = ALL_WEAPONS[id.lowercase()] ?: ALL_WEAPONS["ak47"]!!
}

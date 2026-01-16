package com.github.ssquadteam.talelib.crafting

import com.hypixel.hytale.builtin.crafting.CraftingPlugin
import com.hypixel.hytale.protocol.BenchType
import com.hypixel.hytale.server.core.asset.type.blocktype.config.bench.Bench
import com.hypixel.hytale.server.core.asset.type.item.config.CraftingRecipe
import com.hypixel.hytale.server.core.entity.EntityStore
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.ecs.Ref

// ============================================
// Player Recipe Extensions
// ============================================

fun PlayerRef.learnRecipe(recipeId: String): Boolean {
    val ref = this.ref ?: return false
    if (!ref.isValid) return false
    return try {
        CraftingPlugin.learnRecipe(ref, recipeId, ref.store)
    } catch (e: Exception) {
        false
    }
}

fun PlayerRef.forgetRecipe(recipeId: String): Boolean {
    val ref = this.ref ?: return false
    if (!ref.isValid) return false
    return try {
        CraftingPlugin.forgetRecipe(ref, recipeId, ref.store)
    } catch (e: Exception) {
        false
    }
}

fun PlayerRef.sendKnownRecipes(): Boolean {
    val ref = this.ref ?: return false
    if (!ref.isValid) return false
    return try {
        CraftingPlugin.sendKnownRecipes(ref, ref.store)
        true
    } catch (e: Exception) {
        false
    }
}

// ============================================
// Ref Recipe Extensions
// ============================================

fun Ref<EntityStore>.learnRecipe(recipeId: String): Boolean {
    if (!this.isValid) return false
    return try {
        CraftingPlugin.learnRecipe(this, recipeId, this.store)
    } catch (e: Exception) {
        false
    }
}

fun Ref<EntityStore>.forgetRecipe(recipeId: String): Boolean {
    if (!this.isValid) return false
    return try {
        CraftingPlugin.forgetRecipe(this, recipeId, this.store)
    } catch (e: Exception) {
        false
    }
}

// ============================================
// Recipe Lookup Functions
// ============================================

fun getRecipe(recipeId: String): CraftingRecipe? {
    return try {
        CraftingRecipe.getAssetMap().getAsset(recipeId)
    } catch (e: Exception) {
        null
    }
}

fun recipeExists(recipeId: String): Boolean {
    return getRecipe(recipeId) != null
}

fun getAllRecipeIds(): List<String> {
    return try {
        CraftingRecipe.getAssetMap().keys.toList()
    } catch (e: Exception) {
        emptyList()
    }
}

// ============================================
// Bench Recipe Functions
// ============================================

fun getBenchRecipes(benchId: String): List<CraftingRecipe> {
    return try {
        val bench = Bench.getAssetMap().getAsset(benchId) ?: return emptyList()
        CraftingPlugin.getBenchRecipes(bench)
    } catch (e: Exception) {
        emptyList()
    }
}

fun getBenchRecipes(benchType: BenchType, benchId: String): List<CraftingRecipe> {
    return try {
        CraftingPlugin.getBenchRecipes(benchType, benchId)
    } catch (e: Exception) {
        emptyList()
    }
}

fun getBenchRecipes(benchType: BenchType, benchId: String, category: String): List<CraftingRecipe> {
    return try {
        CraftingPlugin.getBenchRecipes(benchType, benchId, category)
    } catch (e: Exception) {
        emptyList()
    }
}

fun getFieldcraftRecipes(): List<CraftingRecipe> {
    return getBenchRecipes(BenchType.Crafting, CraftingRecipe.FIELDCRAFT_REQUIREMENT)
}

fun getCraftingBenchRecipes(benchId: String): List<CraftingRecipe> {
    return getBenchRecipes(BenchType.Crafting, benchId)
}

fun getProcessingBenchRecipes(benchId: String): List<CraftingRecipe> {
    return getBenchRecipes(BenchType.Processing, benchId)
}

fun getDiagramCraftingRecipes(benchId: String): List<CraftingRecipe> {
    return getBenchRecipes(BenchType.DiagramCrafting, benchId)
}

fun getStructuralCraftingRecipes(benchId: String): List<CraftingRecipe> {
    return getBenchRecipes(BenchType.StructuralCrafting, benchId)
}

// ============================================
// Recipe Category Functions
// ============================================

fun getRecipesForCategory(benchId: String, categoryId: String): Set<String>? {
    return try {
        CraftingPlugin.getAvailableRecipesForCategory(benchId, categoryId)
    } catch (e: Exception) {
        null
    }
}

// ============================================
// Recipe Query Extensions
// ============================================

fun CraftingRecipe.getInputItems(): List<RecipeInput> {
    val inputs = this.input ?: return emptyList()
    return inputs.map { material ->
        RecipeInput(
            itemId = material.itemId,
            resourceTypeId = material.resourceTypeId,
            itemTag = material.itemTag,
            quantity = material.quantity
        )
    }
}

fun CraftingRecipe.getPrimaryOutputItem(): RecipeOutput? {
    val output = this.primaryOutput ?: return null
    return RecipeOutput(
        itemId = output.itemId,
        quantity = output.quantity
    )
}

fun CraftingRecipe.getSecondaryOutputItems(): List<RecipeOutput> {
    val outputs = this.outputs ?: return emptyList()
    return outputs.map { material ->
        RecipeOutput(
            itemId = material.itemId,
            quantity = material.quantity
        )
    }
}

fun CraftingRecipe.getBenchIds(): List<String> {
    val requirements = this.benchRequirement ?: return emptyList()
    return requirements.mapNotNull { it.id }
}

fun CraftingRecipe.getBenchTypes(): List<BenchType> {
    val requirements = this.benchRequirement ?: return emptyList()
    return requirements.mapNotNull { it.type }
}

fun CraftingRecipe.requiresKnowledge(): Boolean {
    return this.isKnowledgeRequired
}

fun CraftingRecipe.getCraftingTime(): Float {
    return this.timeSeconds
}

fun CraftingRecipe.isInstantCraft(): Boolean {
    return this.timeSeconds <= 0f
}

fun CraftingRecipe.isFieldcraftRecipe(): Boolean {
    val requirements = this.benchRequirement ?: return false
    return requirements.any { it.id == CraftingRecipe.FIELDCRAFT_REQUIREMENT }
}

// ============================================
// Data Classes
// ============================================

data class RecipeInput(
    val itemId: String?,
    val resourceTypeId: String?,
    val itemTag: String?,
    val quantity: Int
)

data class RecipeOutput(
    val itemId: String?,
    val quantity: Int
)

// ============================================
// Bench Lookup Functions
// ============================================

fun getBench(benchId: String): Bench? {
    return try {
        Bench.getAssetMap().getAsset(benchId)
    } catch (e: Exception) {
        null
    }
}

fun benchExists(benchId: String): Boolean {
    return getBench(benchId) != null
}

fun getAllBenchIds(): List<String> {
    return try {
        Bench.getAssetMap().keys.toList()
    } catch (e: Exception) {
        emptyList()
    }
}

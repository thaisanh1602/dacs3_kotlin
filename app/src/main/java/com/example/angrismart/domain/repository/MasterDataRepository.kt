package com.example.angrismart.domain.repository

import com.example.angrismart.domain.model.RiceVariant
import com.example.angrismart.domain.model.SeasonTemplate
import com.example.angrismart.utils.Resource
import kotlinx.coroutines.flow.Flow

interface MasterDataRepository {
    fun getRiceVariants(): Flow<Resource<List<RiceVariant>>>
    fun getSeasonTemplates(): Flow<Resource<List<SeasonTemplate>>>
}

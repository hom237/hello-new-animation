package com.daily.new_amime.for_my.annotation

import javax.inject.Qualifier

object RetrofitModule {
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class MainDomainRetrofit

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class SubDomainRetrofit
}
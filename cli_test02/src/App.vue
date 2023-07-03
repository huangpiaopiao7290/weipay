<template>
    <div id="app">
        <!-- 导航栏 -->
        <AppHeader></AppHeader>
        <!-- 展示区 -->
        <router-view></router-view>
        <!-- 尾部 -->
        <!-- <AppFooter></AppFooter> -->

    </div>
</template>

<script>
    import Orders from './views/Orders.vue'
    import List from './views/List.vue'
    import Home from './views/Home.vue'
    import Mine from './views/Mine.vue'
    import AppHeader from './views/AppHeader.vue'
    import AppFooter from './views/AppFooter.vue'

    export default {
        name: 'App',
        components:{
            AppHeader,
            AppFooter,
            Orders,
            List,
            Home,
            Mine

        },

    }
</script>

<style>

</style>
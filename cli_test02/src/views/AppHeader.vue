<template>
    <!-- 公共头部 -->
    <div class="container">
        <nav>
            <router-link to="/home">首页</router-link>
            <router-link to="/list">课程</router-link>
            <router-link to="/recordTime">计时</router-link>
            <router-link to="/orders">订单管理</router-link>
            <router-link to="/mine">我的</router-link>

            <div class="animation start-home"></div>
        </nav>

    </div>
</template>

<style lang="css" scoped>

    .container {
        width           : 600px;
        height          : 60px;
        display         : flex;
        justify-content : center;
        align-items     : center;
        font-family     : 'Montserrat', sans-serif, Arial, 'Microsoft Yahei';
        margin: auto;

    }

    nav {
        position        : relative;
        width           : 590px;
        height          : 50px;
        background-color: var(--nav-background-color);
        border-radius   : 8px;
        box-shadow      : 0 2px 3px rgba(0, 0, 0, 0.1);
    }

    nav a {
        position   : relative;
        display    : block;
        float      : left;
        font-size  : 15px;
        line-height: 50px;

        height             : 50px;
        color              : black;
        text-transform     : uppercase;
        text-decoration    : none;
        text-align         : center;
        /* background-color: #c1cbc6; */
        z-index            : 10;
    }

    nav a:nth-child(1) {
        width: 100px;
    }

    nav a:nth-child(2) {
        width: 110px;
    }

    nav a:nth-child(3) {
        width: 100px;
    }

    nav a:nth-child(4) {
        width: 160px;
    }

    nav a:nth-child(5) {
        width: 120px;
    }

    nav .animation {
        position        : absolute;
        height          : 100%;
        background-color: #F97F51;
        z-index         : 9;
        border-radius   : 8px;
        transition: all 0.3s;
    }

    .start-home,
    nav a:nth-child(1):hover~.animation {
        width: 100px;
        left : 0px;
    }
    .start-about,
    nav a:nth-child(2):hover~.animation {
        width: 110px;
        left : 100px;
    }
    .start-blog,
    nav a:nth-child(3):hover~.animation {
        width: 100px;
        left : 210px;
    }
    .start-protfolio,
    nav a:nth-child(4):hover~.animation {
        width: 160px;
        left : 310px;
    }
    .start-contact,
    nav a:nth-child(5):hover~.animation {
        width: 120px;
        left : 470px;
    }
</style>
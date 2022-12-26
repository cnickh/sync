//package daemon.dev.field.fragments.compose
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.wrapContentHeight
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.LocalTextStyle
//import androidx.compose.material.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.draw.shadow
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.constraintlayout.compose.ConstraintLayout
//import androidx.constraintlayout.compose.Dimension
//
//import daemon.dev.field.fragments.compose.ui.theme.FieldTheme
//
////    override fun onCreate(savedInstanceState: Bundle?) {
////        super.onCreate(savedInstanceState)
////        setContent {
////            FieldTheme {
////                // A surface container using the 'background' color from the theme
//////                Surface(
//////                    modifier = Modifier.fillMaxSize(),
//////                    color = MaterialTheme.colors.background
//////                ) {
//////                    Greeting("Android")
//////                }
////
////                AndroidPreview_Profile()
////
////            }
////        }
////    }
//
//
//    @Composable
//    fun Greeting(name: String) {
//        Text(text = "Hello $name!")
//    }
//
//    @Preview(showBackground = true)
//    @Composable
//    fun DefaultPreview() {
//        FieldTheme {
//            Greeting("Android")
//        }
//    }
//
//
//    @Composable()
//    fun Component_4(modifier: Modifier = Modifier) {
//        ConstraintLayout(modifier = modifier.shadow(4.0.dp)) {
//            val (Rectangle_25, dots_2) = createRefs()
//
//
//            Box(
//                Modifier.clip(RoundedCornerShape(32.0.dp)).size(360.0.dp, 59.0.dp)
//                    .background(Color(0.84f, 0.72f, 0.95f, 1.0f)).constrainAs(Rectangle_25) {
//                    centerTo(parent)
//                    width = Dimension.fillToConstraints
//                    height = Dimension.fillToConstraints
//                }) {}
//
//            Box(Modifier.size(64.0.dp, 38.0.dp).constrainAs(dots_2) {
//                linkTo(parent.start, parent.end, bias = 0.5f)
//                linkTo(parent.top, parent.bottom, bias = 0.52f)
//                width = Dimension.percent(0.18f)
//                height = Dimension.percent(0.64f)
//            }) {}
//
//        }
//    }
//
//    @Composable()
//    @Preview()
//    fun AndroidPreview_Profile() {
//        Box(Modifier.size(360.dp, 640.dp)) {
//            Profile()
//        }
//    }
//
//    @Composable()
//    fun Profile() {
//        ConstraintLayout(
//            //modifier = Modifier.background(Color(0.0f, 0.0f, 0.0f, 1.0f)).fillMaxSize()
//        ) {
//            val (Rectangle_12, Rectangle_13, Rectangle_14, bt_1, bt_2, bt_3, bt_4, wifi_1, wifi_2, alias, Visible_, Share_, Forward_, id_sig_, pencil_1, Component_4) = createRefs()
//
//
//            Box(
//                Modifier.clip(RoundedCornerShape(20.0.dp)).size(340.0.dp, 286.0.dp)
//                    .background(Color(0.65f, 0.65f, 0.65f, 1.0f)).constrainAs(Rectangle_12) {
//                    start.linkTo(parent.start, 10.0.dp)
//                    top.linkTo(parent.top, 25.0.dp)
//                    width = Dimension.value(340.0.dp)
//                    height = Dimension.value(286.0.dp)
//                }) {}
//
//            Box(
//                Modifier.clip(RoundedCornerShape(20.0.dp)).size(340.0.dp, 72.0.dp)
//                    .background(Color(0.65f, 0.65f, 0.65f, 1.0f)).constrainAs(Rectangle_12) {
//                    start.linkTo(parent.start, 10.0.dp)
//                    top.linkTo(parent.top, 317.0.dp)
//                    width = Dimension.value(340.0.dp)
//                    height = Dimension.value(72.0.dp)
//                }) {}
//
//            Box(
//                Modifier.clip(RoundedCornerShape(20.0.dp)).size(340.0.dp, 72.0.dp)
//                    .background(Color(0.65f, 0.65f, 0.65f, 1.0f)).constrainAs(Rectangle_13) {
//                    start.linkTo(parent.start, 10.0.dp)
//                    top.linkTo(parent.top, 394.0.dp)
//                    width = Dimension.value(340.0.dp)
//                    height = Dimension.value(72.0.dp)
//                }) {}
//
//            Box(
//                Modifier.clip(RoundedCornerShape(20.0.dp)).size(340.0.dp, 72.0.dp)
//                    .background(Color(0.65f, 0.65f, 0.65f, 1.0f)).constrainAs(Rectangle_14) {
//                    start.linkTo(parent.start, 10.0.dp)
//                    top.linkTo(parent.top, 472.0.dp)
//                    width = Dimension.value(340.0.dp)
//                    height = Dimension.value(72.0.dp)
//                }) {}
//
//            Box(Modifier.size(33.0.dp, 31.0.dp).constrainAs(bt_1) {
//                start.linkTo(parent.start, 296.0.dp)
//                top.linkTo(parent.top, 337.0.dp)
//                width = Dimension.value(33.0.dp)
//                height = Dimension.value(31.0.dp)
//            }) {}
//
//            Box(Modifier.size(33.0.dp, 31.0.dp).constrainAs(bt_2) {
//                start.linkTo(parent.start, 296.0.dp)
//                top.linkTo(parent.top, 49.0.dp)
//                width = Dimension.value(33.0.dp)
//                height = Dimension.value(31.0.dp)
//            }) {}
//
//            Box(Modifier.size(33.0.dp, 31.0.dp).constrainAs(bt_3) {
//                start.linkTo(parent.start, 294.0.dp)
//                top.linkTo(parent.top, 492.0.dp)
//                width = Dimension.value(33.0.dp)
//                height = Dimension.value(31.0.dp)
//            }) {}
//
//            Box(Modifier.size(33.0.dp, 31.0.dp).constrainAs(bt_4) {
//                start.linkTo(parent.start, 294.0.dp)
//                top.linkTo(parent.top, 415.0.dp)
//                width = Dimension.value(33.0.dp)
//                height = Dimension.value(31.0.dp)
//            }) {}
//
//            Box(Modifier.size(35.0.dp, 26.0.dp).constrainAs(wifi_1) {
//                start.linkTo(parent.start, 256.0.dp)
//                top.linkTo(parent.top, 55.0.dp)
//                width = Dimension.value(35.0.dp)
//                height = Dimension.value(26.0.dp)
//            }) {}
//
//            Box(Modifier.size(35.0.dp, 26.0.dp).constrainAs(wifi_2) {
//                start.linkTo(parent.start, 256.0.dp)
//                top.linkTo(parent.top, 340.0.dp)
//                width = Dimension.value(35.0.dp)
//                height = Dimension.value(26.0.dp)
//            }) {}
//
//            Text(
//                "alias#12345",
//                Modifier.wrapContentHeight(Alignment.CenterVertically).constrainAs(alias) {
//                    start.linkTo(parent.start, 81.0.dp)
//                    top.linkTo(parent.top, 52.0.dp)
//                    width = Dimension.value(199.0.dp)
//                    height = Dimension.value(29.0.dp)
//                },
//                style = LocalTextStyle.current.copy(
//                    color = Color(0.0f, 0.0f, 0.0f, 1.0f),
//                    textAlign = TextAlign.Left,
//                    fontSize = 24.0.sp
//                )
//            )
//
////        Text("  alias#12345", Modifier.wrapContentHeight(Alignment.CenterVertically).constrainAs(alias#12345) {
////            start.linkTo(parent.start, 74.0.dp)
////            top.linkTo(parent.top, 338.0.dp)
////            width = Dimension.value(199.0.dp)
////            height = Dimension.value(29.0.dp)
////        }, style = LocalTextStyle.current.copy(color = Color(0.0f, 0.0f, 0.0f, 1.0f), textAlign = TextAlign.Left, fontSize = 24.0.sp))
////
////        Text("  alias#12345", Modifier.wrapContentHeight(Alignment.CenterVertically).constrainAs(alias#12345) {
////            start.linkTo(parent.start, 74.0.dp)
////            top.linkTo(parent.top, 417.0.dp)
////            width = Dimension.value(199.0.dp)
////            height = Dimension.value(29.0.dp)
////        }, style = LocalTextStyle.current.copy(color = Color(0.0f, 0.0f, 0.0f, 1.0f), textAlign = TextAlign.Left, fontSize = 24.0.sp))
////
////        Text("  alias#12345", Modifier.wrapContentHeight(Alignment.CenterVertically).constrainAs(alias#12345) {
////            start.linkTo(parent.start, 74.0.dp)
////            top.linkTo(parent.top, 493.0.dp)
////            width = Dimension.value(199.0.dp)
////            height = Dimension.value(29.0.dp)
////        }, style = LocalTextStyle.current.copy(color = Color(0.0f, 0.0f, 0.0f, 1.0f), textAlign = TextAlign.Left, fontSize = 24.0.sp))
//
//
//            Text(
//                "Visible?",
//                Modifier.wrapContentHeight(Alignment.CenterVertically).constrainAs(Visible_) {
//                    start.linkTo(parent.start, 25.0.dp)
//                    top.linkTo(parent.top, 117.0.dp)
//                    width = Dimension.value(302.0.dp)
//                    height = Dimension.value(32.0.dp)
//                },
//                style = LocalTextStyle.current.copy(
//                    color = Color(0.0f, 0.0f, 0.0f, 1.0f),
//                    textAlign = TextAlign.Center,
//                    fontSize = 16.0.sp
//                )
//            )
//
//            Text(
//                "Share?",
//                Modifier.wrapContentHeight(Alignment.CenterVertically).constrainAs(Share_) {
//                    start.linkTo(parent.start, 26.0.dp)
//                    top.linkTo(parent.top, 164.0.dp)
//                    width = Dimension.value(302.0.dp)
//                    height = Dimension.value(32.0.dp)
//                },
//                style = LocalTextStyle.current.copy(
//                    color = Color(0.0f, 0.0f, 0.0f, 1.0f),
//                    textAlign = TextAlign.Center,
//                    fontSize = 16.0.sp
//                )
//            )
//
//            Text(
//                "Forward?",
//                Modifier.wrapContentHeight(Alignment.CenterVertically).constrainAs(Forward_) {
//                    start.linkTo(parent.start, 27.0.dp)
//                    top.linkTo(parent.top, 211.0.dp)
//                    width = Dimension.value(302.0.dp)
//                    height = Dimension.value(32.0.dp)
//                },
//                style = LocalTextStyle.current.copy(
//                    color = Color(0.0f, 0.0f, 0.0f, 1.0f),
//                    textAlign = TextAlign.Center,
//                    fontSize = 16.0.sp
//                )
//            )
//
//            Text(
//                "id/sig:",
//                Modifier.wrapContentHeight(Alignment.CenterVertically).constrainAs(id_sig_) {
//                    start.linkTo(parent.start, 28.0.dp)
//                    top.linkTo(parent.top, 258.0.dp)
//                    width = Dimension.value(302.0.dp)
//                    height = Dimension.value(32.0.dp)
//                },
//                style = LocalTextStyle.current.copy(
//                    color = Color(0.0f, 0.0f, 0.0f, 1.0f),
//                    textAlign = TextAlign.Center,
//                    fontSize = 16.0.sp
//                )
//            )
//
//            Box(Modifier.shadow(4.0.dp).size(16.0.dp, 16.0.dp).constrainAs(pencil_1) {
//                start.linkTo(parent.start, 225.0.dp)
//                top.linkTo(parent.top, 42.0.dp)
//                width = Dimension.value(16.0.dp)
//                height = Dimension.value(16.0.dp)
//            }) {}
//
//            Component_4(Modifier.constrainAs(Component_4) {
//                start.linkTo(parent.start, 0.0.dp)
//                bottom.linkTo(parent.bottom, 20.0.dp)
//                width = Dimension.value(360.0.dp)
//                height = Dimension.value(59.0.dp)
//            })
//        }
//    }

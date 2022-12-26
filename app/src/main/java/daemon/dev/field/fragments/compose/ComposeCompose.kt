package daemon.dev.field.fragments.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension

@Composable()
@Preview()
fun AndroidPreview_Android_Large___11() {
    //Box(Modifier.size(360.dp, 640.dp)) {
        Android_Large___11()
   // }
}

@Composable()
fun Android_Large___11() {
    ConstraintLayout(modifier = Modifier.background(Color(1.0f, 1.0f, 1.0f, 1.0f)).fillMaxSize()) {
        val (Frame_197, Frame_1000001967, Frame_1000001968, Frame_1000001966, Group_196, mi_options_vertical, Frame_1000001982) = createRefs()


        ConstraintLayout(modifier = Modifier.constrainAs(Frame_197) {
            start.linkTo(parent.start, 1.0.dp)
            top.linkTo(parent.top, 72.0.dp)
            width = Dimension.value(345.0.dp)
            height = Dimension.value(44.0.dp)
        }) {
            val (Group_199) = createRefs()


            Box() {
                Text("To", Modifier.wrapContentHeight(Alignment.Top),
                    style = LocalTextStyle.current.copy(
                        color = Color(0.2f, 0.2f, 0.2f, 1.0f),
                        textAlign = TextAlign.Left, fontSize = 15.0.sp))


                Text("@Username", Modifier.wrapContentHeight(Alignment.Top),
                    style = LocalTextStyle.current.copy(
                        color = Color(0.2f, 0.2f, 0.2f, 1.0f),
                        textAlign = TextAlign.Left, fontSize = 14.0.sp))


//                ConstraintLayout(modifier = Modifier) {
//                    val (Vector, Vector) = createRefs()
//                    /* raw vector Vector should have an export setting */
//                    /* raw vector Vector should have an export setting */
//                }
            }
        }
        ConstraintLayout(modifier = Modifier.constrainAs(Frame_1000001967) {
            start.linkTo(parent.start, 0.0.dp)
            top.linkTo(parent.top, 123.0.dp)
            width = Dimension.value(345.0.dp)
            height = Dimension.value(44.0.dp)
        }) {
            val (Group_199) = createRefs()


            Box() {
                Text("Subject", Modifier.wrapContentHeight(Alignment.Top),
                    style = LocalTextStyle.current.copy(
                        color = Color(0.2f, 0.2f, 0.2f, 1.0f),
                        textAlign = TextAlign.Left, fontSize = 15.0.sp))

                Text("Yesterday 17:03", Modifier.wrapContentHeight(Alignment.Top),
                    style = LocalTextStyle.current.copy(
                        color = Color(0.2f, 0.2f, 0.2f, 1.0f),
                        textAlign = TextAlign.Right, fontSize = 12.0.sp))

//                ConstraintLayout(modifier = Modifier) {
//                    val (Vector, Vector) = createRefs()
//                    /* raw vector Vector should have an export setting */
//                    /* raw vector Vector should have an export setting */
//                }
            }
        }
        ConstraintLayout(modifier = Modifier.constrainAs(Frame_1000001968) {
            start.linkTo(parent.start, 0.0.dp)
            top.linkTo(parent.top, 174.0.dp)
            width = Dimension.value(345.0.dp)
            height = Dimension.value(44.0.dp)
        }) {
            val (Group_199) = createRefs()


            Box() {
                Text("Compose message...", Modifier.wrapContentHeight(Alignment.Top),
                    style = LocalTextStyle.current.copy(
                        color = Color(0.2f, 0.2f, 0.2f, 1.0f),
                        textAlign = TextAlign.Left, fontSize = 15.0.sp))

                Text("Yesterday 17:03", Modifier.wrapContentHeight(Alignment.Top),
                    style = LocalTextStyle.current.copy(
                        color = Color(0.2f, 0.2f, 0.2f, 1.0f),
                        textAlign = TextAlign.Right, fontSize = 12.0.sp))

//                ConstraintLayout(modifier = Modifier) {
//                    val (Vector, Vector) = createRefs()
//                    /* raw vector Vector should have an export setting */
//                    /* raw vector Vector should have an export setting */
//                }
            }
        }
        ConstraintLayout(modifier = Modifier
            .shadow(26.0.dp, /*opacity = 0.99f,*/ )
            .background(Color(1.0f, 1.0f, 1.0f, 1.0f))
            .constrainAs(Frame_1000001966) {

            start.linkTo(parent.start, 0.0.dp)
            top.linkTo(parent.top, 740.0.dp)
            width = Dimension.value(360.0.dp)
            height = Dimension.value(60.0.dp)

        }) {
            val (mdi_user_circle, Chat, user, Line_1) = createRefs()


            ConstraintLayout(modifier = Modifier.constrainAs(mdi_user_circle) {
                start.linkTo(parent.start, 43.0.dp)
                top.linkTo(parent.top, 13.0.dp)
                width = Dimension.value(34.0.dp)
                height = Dimension.value(34.0.dp)
            }) {
                val (Vector) = createRefs()
                /* raw vector Vector should have an export setting */
            }
            ConstraintLayout(modifier = Modifier.constrainAs(Chat) {
                start.linkTo(parent.start, 163.0.dp)
                top.linkTo(parent.top, 13.0.dp)
                width = Dimension.value(34.0.dp)
                height = Dimension.value(34.0.dp)
            }) {
//                val (Vector, Vector) = createRefs()

                /* raw vector Vector should have an export setting */
            /* raw vector Vector should have an export setting */
            }
            ConstraintLayout(modifier = Modifier.constrainAs(user) {
                start.linkTo(parent.start, 283.0.dp)
                top.linkTo(parent.top, 13.0.dp)
                width = Dimension.value(34.0.dp)
                height = Dimension.value(34.0.dp)
            }) {
//                val (Vector, Vector) = createRefs()


                /* raw vector Vector should have an export setting */
/* raw vector Vector should have an export setting */
            }

        }
        Box() {
            Row(Modifier.height(99.0.dp), horizontalArrangement = Arrangement.spacedBy(10.0.dp), verticalAlignment = Alignment.Bottom) {

                ConstraintLayout(modifier = Modifier.shadow(8.0.dp, /* opacity = 0.99f, */ shape = RoundedCornerShape(7.0.dp)).shadow(9.0.dp, /* opacity = 0.99f, */ shape = RoundedCornerShape(7.0.dp)).clip(RoundedCornerShape(7.0.dp)).background(Color(1.0f, 1.0f, 1.0f, 1.0f)).size(149.0.dp, 99.0.dp).fillMaxHeight()) {
                    val (Frame_1000001963, Text) = createRefs()


                    ConstraintLayout(modifier = Modifier.clip(RoundedCornerShape(8.0.dp)).constrainAs(Frame_1000001963) {
                        start.linkTo(parent.start, 11.0.dp)
                        top.linkTo(parent.top, 14.0.dp)
                        width = Dimension.value(128.0.dp)
                        height = Dimension.value(34.0.dp)
                    }) {
                        val (Text) = createRefs()


                        Text("New Shift", Modifier.wrapContentHeight(Alignment.Top).constrainAs(Text) {
                            start.linkTo(parent.start, 9.0.dp)
                            top.linkTo(parent.top, 8.0.dp)
                            width = Dimension.value(65.0.dp)
                            height = Dimension.value(18.0.dp)
                        }, style = LocalTextStyle.current.copy(color = Color(0.07f, 0.07f, 0.07f, 1.0f), textAlign = TextAlign.Left, fontSize = 14.0.sp))

                    }
                    Text("New Employer", Modifier.wrapContentHeight(Alignment.Top).constrainAs(Text) {
                        start.linkTo(parent.start, 20.0.dp)
                        top.linkTo(parent.top, 64.0.dp)
                        width = Dimension.value(96.0.dp)
                        height = Dimension.value(18.0.dp)
                    }, style = LocalTextStyle.current.copy(color = Color(0.07f, 0.07f, 0.07f, 1.0f), textAlign = TextAlign.Left, fontSize = 14.0.sp))

                }

            }
            Box() {

                ConstraintLayout(modifier = Modifier) {
//                    val (Vector, Vector) = createRefs()


                    /* raw vector Vector should have an export setting */
/* raw vector Vector should have an export setting */
                }
            }
        }
        ConstraintLayout(modifier = Modifier.constrainAs(mi_options_vertical) {
            start.linkTo(parent.start, 331.0.dp)
            top.linkTo(parent.top, 56.0.dp)
            width = Dimension.value(20.0.dp)
            height = Dimension.value(20.0.dp)
        }) {
            val (Vector) = createRefs()


            /* raw vector Vector should have an export setting */
        }
        Column(Modifier.width(360.0.dp).constrainAs(Frame_1000001982) {
            start.linkTo(parent.start, 1.0.dp)
            top.linkTo(parent.top, 0.0.dp)
            width = Dimension.value(360.0.dp)
            height = Dimension.value(47.0.dp)
        }, verticalArrangement = Arrangement.spacedBy(10.0.dp), horizontalAlignment = Alignment.Start) {
            Spacer(modifier = Modifier.height(9.0.dp))
            Box() {
                Text("Compose", Modifier.wrapContentHeight(Alignment.Top), style = LocalTextStyle.current.copy(color = Color(0.0f, 0.0f, 0.0f, 1.0f), textAlign = TextAlign.Center, fontSize = 24.0.sp))

                Box() {
                    ConstraintLayout(modifier = Modifier) {
//                        val (Vector, Vector) = createRefs()


                        /* raw vector Vector should have an export setting */
/* raw vector Vector should have an export setting */
                    }
                    ConstraintLayout(modifier = Modifier) {
//                        val (Vector, Vector) = createRefs()


                        /* raw vector Vector should have an export setting */
/* raw vector Vector should have an export setting */
                    }
                }
                ConstraintLayout(modifier = Modifier) {
//                    val (Vector, Vector) = createRefs()


                    /* raw vector Vector should have an export setting */
/* raw vector Vector should have an export setting */
                }
            }
            Spacer(modifier = Modifier.height(9.0.dp))
        }
    }
}
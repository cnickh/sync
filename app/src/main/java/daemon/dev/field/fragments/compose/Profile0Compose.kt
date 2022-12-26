package daemon.dev.field.fragments.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
fun Component_4(modifier: Modifier = Modifier) {
    ConstraintLayout(modifier = modifier.shadow(4.0.dp )) {
        val (Rectangle_25, dots_2) = createRefs()


        Box(Modifier.clip(RoundedCornerShape(32.0.dp)).size(360.0.dp, 59.0.dp).background(Color(0.84f, 0.72f, 0.95f, 1.0f)).constrainAs(Rectangle_25) {
            centerTo(parent)
            width = Dimension.fillToConstraints
            height = Dimension.fillToConstraints
        }){}

        Box(Modifier.size(64.0.dp, 38.0.dp).constrainAs(dots_2) {
            linkTo(parent.start, parent.end, bias = 0.5f)
            linkTo(parent.top, parent.bottom, bias = 0.52f)
            width = Dimension.percent(0.18f)
            height = Dimension.percent(0.64f)
        }){}

    }
}

@Composable()
@Preview()
fun AndroidPreview_Profile() {
    Box(Modifier.size(360.dp, 640.dp)) {

        Column{
            Box(Modifier.shadow(4.0.dp,
                shape = RoundedCornerShape(20.0.dp))
                .clip(RoundedCornerShape(20.0.dp))
                .size(340.0.dp, 161.0.dp)
                .background(Color(0.5f, 0.5f, 0.5f, 1.0f)) )

            Box(Modifier.shadow(4.0.dp,
                shape = RoundedCornerShape(20.0.dp))
                .clip(RoundedCornerShape(20.0.dp))
                .size(340.0.dp, 72.0.dp)
                .background(Color(0.5f, 0.5f, 0.5f, 1.0f)) )

            Text("Hello world!")
        }

//        Profile()
    }
}


@Composable()
fun Profile() {
    ConstraintLayout(modifier = Modifier.background(Color(1.0f, 1.0f, 1.0f, 1.0f)).fillMaxSize()) {
        val (Rectangle_12, Rectangle_13, Rectangle_14, alias, Ellipse_4, Ellipse_5, Ellipse_6, id_sig_, pencil_1, logo_clear_1, Component_4) = createRefs()


        Box(Modifier.shadow(4.0.dp, shape = RoundedCornerShape(20.0.dp)).clip(RoundedCornerShape(20.0.dp)).size(340.0.dp, 161.0.dp).background(Color(0.5f, 0.5f, 0.5f, 1.0f)).constrainAs(Rectangle_12) {
            start.linkTo(parent.start, 10.0.dp)
            top.linkTo(parent.top, 16.0.dp)
            width = Dimension.value(340.0.dp)
            height = Dimension.value(161.0.dp)
        }){}

        Box(Modifier.clip(RoundedCornerShape(20.0.dp)).size(340.0.dp, 72.0.dp).background(Color(0.92f, 0.92f, 0.92f, 1.0f)).constrainAs(Rectangle_12) {
            start.linkTo(parent.start, 10.0.dp)
            top.linkTo(parent.top, 195.0.dp)
            width = Dimension.value(340.0.dp)
            height = Dimension.value(72.0.dp)
        }){}

        Box(Modifier.clip(RoundedCornerShape(20.0.dp)).size(340.0.dp, 72.0.dp).background(Color(0.92f, 0.92f, 0.92f, 1.0f)).constrainAs(Rectangle_13) {
            start.linkTo(parent.start, 10.0.dp)
            top.linkTo(parent.top, 272.0.dp)
            width = Dimension.value(340.0.dp)
            height = Dimension.value(72.0.dp)
        }){}

        Box(Modifier.clip(RoundedCornerShape(20.0.dp)).size(340.0.dp, 72.0.dp).background(Color(0.92f, 0.92f, 0.92f, 1.0f)).constrainAs(Rectangle_14) {
            start.linkTo(parent.start, 10.0.dp)
            top.linkTo(parent.top, 350.0.dp)
            width = Dimension.value(340.0.dp)
            height = Dimension.value(72.0.dp)
        }){}

        Text("  alias#12345", Modifier.wrapContentHeight(Alignment.CenterVertically).constrainAs(alias) {
            start.linkTo(parent.start, 47.0.dp)
            top.linkTo(parent.top, 104.0.dp)
            width = Dimension.value(192.0.dp)
            height = Dimension.value(21.0.dp)
        }, style = LocalTextStyle.current.copy(color = Color(0.0f, 0.0f, 0.0f, 1.0f), textAlign = TextAlign.Left, fontSize = 18.0.sp))

//        Text("  alias#12345", Modifier.wrapContentHeight(Alignment.CenterVertically).constrainAs(alias#12345) {
//            start.linkTo(parent.start, 74.0.dp)
//            top.linkTo(parent.top, 216.0.dp)
//            width = Dimension.value(199.0.dp)
//            height = Dimension.value(29.0.dp)
//        }, style = LocalTextStyle.current.copy(color = Color(0.0f, 0.0f, 0.0f, 1.0f), textAlign = TextAlign.Left, fontSize = 24.0.sp))
//
//        Text("  alias#12345", Modifier.wrapContentHeight(Alignment.CenterVertically).constrainAs(alias#12345) {
//            start.linkTo(parent.start, 74.0.dp)
//            top.linkTo(parent.top, 295.0.dp)
//            width = Dimension.value(199.0.dp)
//            height = Dimension.value(29.0.dp)
//        }, style = LocalTextStyle.current.copy(color = Color(0.0f, 0.0f, 0.0f, 1.0f), textAlign = TextAlign.Left, fontSize = 24.0.sp))
//
//        Text("  alias#12345", Modifier.wrapContentHeight(Alignment.CenterVertically).constrainAs(alias#12345) {
//            start.linkTo(parent.start, 74.0.dp)
//            top.linkTo(parent.top, 371.0.dp)
//            width = Dimension.value(199.0.dp)
//            height = Dimension.value(29.0.dp)
//        }, style = LocalTextStyle.current.copy(color = Color(0.0f, 0.0f, 0.0f, 1.0f), textAlign = TextAlign.Left, fontSize = 24.0.sp))
//



        Text("id/sig:", Modifier.wrapContentHeight(Alignment.CenterVertically).constrainAs(id_sig_) {
            start.linkTo(parent.start, 54.0.dp)
            top.linkTo(parent.top, 135.0.dp)
            width = Dimension.value(302.0.dp)
            height = Dimension.value(32.0.dp)
        }, style = LocalTextStyle.current.copy(color = Color(0.0f, 0.0f, 0.0f, 1.0f), textAlign = TextAlign.Left, fontSize = 16.0.sp))

        Box(Modifier.shadow(4.0.dp, ).size(16.0.dp, 16.0.dp).constrainAs(pencil_1) {
            start.linkTo(parent.start, 157.0.dp)
            top.linkTo(parent.top, 96.0.dp)
            width = Dimension.value(16.0.dp)
            height = Dimension.value(16.0.dp)
        }){}

        Box(Modifier.size(57.0.dp, 54.0.dp).constrainAs(logo_clear_1) {
            linkTo(parent.start, parent.end, bias = 0.49f)
            linkTo(parent.top, parent.bottom, bias = 0.03f)
            width = Dimension.percent(0.16f)
            height = Dimension.percent(0.06f)
        }){}

        Component_4(Modifier.constrainAs(Component_4) {
            start.linkTo(parent.start, 0.0.dp)
            bottom.linkTo(parent.bottom, 20.0.dp)
            width = Dimension.value(360.0.dp)
            height = Dimension.value(59.0.dp)
        })
    }
}
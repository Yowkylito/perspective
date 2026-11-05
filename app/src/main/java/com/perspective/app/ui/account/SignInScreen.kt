package com.perspective.app.ui.account

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.perspective.app.data.Session
import com.perspective.app.ui.components.PerspectiveButton
import com.perspective.app.ui.theme.Space

/**
 * Field height, matched to [PerspectiveButton]'s own 48dp.
 *
 * The storefront's search box is 34dp, but that number comes from the 56dp bar
 * it has to sit inside. Here the field and the control are two hairline
 * rectangles stacked a few millimetres apart, and two different heights would
 * read as two different systems meeting rather than one.
 */
private val FieldHeight = 48.dp

/**
 * Sign in.
 *
 * One field, one control, no password — which is the honest shape rather than a
 * reduced one. There is no backend behind this app, so a password box would be
 * asking for a secret that nothing can check and nothing should keep: security
 * theatre, which is worse than none, and it would make this the one screen in
 * the app that lies about what it does. [Session] takes the address and derives
 * a name from it. That is the whole of it, and the copy says so.
 *
 * **Validation is the two things that can actually be known here.** Non-blank,
 * and it contains an `@`. Anything stricter is a regex pretending to know which
 * addresses exist — the usual ones turn away perfectly deliverable mail and
 * still wave `a@b` through — and there is no server to ask instead. The control
 * stays unavailable until both hold.
 *
 * **No error line, deliberately.** A message that appears while someone is
 * halfway through typing their address tells them they are wrong before they
 * have finished speaking. The disabled control carries the same information
 * without the accusation, and it withdraws it the moment the address is
 * complete.
 *
 * The field is the storefront header's search box with two things changed, both
 * on purpose: it stands at [FieldHeight] rather than 34dp, and its text is set
 * in the detail voice rather than tracked mono caps — what is typed here is a
 * lowercase address, not a line of chrome, and tracked capitals would fight it
 * the whole way. The construction underneath is the same hairline rectangle,
 * the same hand-placed placeholder, the same cursor.
 *
 * Nothing on this page animates. The only state change is the control becoming
 * available, which [PerspectiveButton] already renders — and a page that
 * assembled itself on arrival would be motion with nothing to say. The tick on
 * the press comes from the button, which now carries it for every call site.
 */
@Composable
fun SignInScreen(
    onSignedIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Saveable rather than remembered: rotating with a half-typed address and
    // getting an empty box back is the sort of loss people blame themselves
    // for. The plain-string field is enough here — there is no selection worth
    // restoring in a value that gets replaced wholesale when it is wrong.
    var typed by rememberSaveable { mutableStateOf("") }

    val address = typed.trim()
    val valid = address.isNotEmpty() && address.contains('@')

    val enter = {
        Session.signIn(address)
        onSignedIn()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            // Edge to edge means the IME draws over the content, and this page
            // is too short to scroll out from under it -- the keyboard opened
            // on top of CONTINUE and the only way through was the Go key.
            .imePadding()
            .padding(horizontal = Space.Gutter),
    ) {
        Spacer(Modifier.height(Space.Lg))
        Text(text = "SIGN IN", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(Space.Sm))
        Text(
            text = "There is no password. The address is what an order is filed " +
                "under, and it is all this keeps.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(Space.Xl))
        // The label stays when the placeholder goes. A single field is legible
        // from its placeholder only while it is empty, which is the one moment
        // nobody needs telling.
        Text(text = "EMAIL ADDRESS", style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(Space.Md))
        EmailField(
            value = typed,
            onValue = { typed = it },
            // The keyboard's own key is the same commitment as the control, so
            // it is held to the same condition rather than being a second way
            // in that skips the check.
            onGo = { if (valid) enter() },
        )

        Spacer(Modifier.height(Space.Lg))
        PerspectiveButton(
            label = "CONTINUE",
            onClick = enter,
            enabled = valid,
        )

        Spacer(Modifier.height(Space.Xxl))
    }
}

/**
 * The app's second text input, built as the first one is.
 *
 * Square corners and a hairline rule — a rounded or filled field would be the
 * one soft edge in an interface that has no radius anywhere. The placeholder is
 * a sibling `Text` rather than a decoration box, which is the same construction
 * the search field uses and is why it can carry its own style.
 *
 * The example address is a plain one. A real-looking name in a placeholder is
 * somebody's address, and this is the wrong place to borrow one.
 */
@Composable
private fun EmailField(
    value: String,
    onValue: (String) -> Unit,
    onGo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(FieldHeight)
            .border(Space.Hairline, scheme.outlineVariant)
            .padding(horizontal = Space.Sm + Space.Xs),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (value.isEmpty()) {
            Text(
                text = "name@example.com",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValue,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Go,
            ),
            keyboardActions = KeyboardActions(onGo = { onGo() }),
            textStyle = LocalTextStyle.current.merge(
                MaterialTheme.typography.bodyMedium.copy(color = scheme.onBackground),
            ),
            cursorBrush = SolidColor(scheme.onBackground),
            modifier = Modifier
                .fillMaxWidth()
                // The micro-label above is a sibling node, so without this the
                // field announces as an edit box with no name at all.
                .semantics { contentDescription = "Email address" },
        )
    }
}

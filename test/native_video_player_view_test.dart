import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:native_video_player/native_video_player.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  testWidgets('removes the native view while a modal route covers it',
      (tester) async {
    debugDefaultTargetPlatformOverride = TargetPlatform.iOS;
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(
      SystemChannels.platform_views,
      (_) async => null,
    );
    try {
      await tester.pumpWidget(
        MaterialApp(
          home: Builder(
            builder: (context) => Scaffold(
              body: Column(
                children: [
                  Expanded(
                    child: NativeVideoPlayerView(onViewReady: (_) {}),
                  ),
                  ElevatedButton(
                    onPressed: () => showModalBottomSheet<void>(
                      context: context,
                      builder: (_) => const Text('Filter sheet'),
                    ),
                    child: const Text('Open filter'),
                  ),
                ],
              ),
            ),
          ),
        ),
      );

      expect(find.byType(UiKitView), findsOneWidget);

      await tester.tap(find.text('Open filter'));
      await tester.pumpAndSettle();

      expect(find.text('Filter sheet'), findsOneWidget);
      expect(find.byType(UiKitView), findsNothing);

      await tester.tapAt(const Offset(10, 10));
      await tester.pumpAndSettle();

      expect(find.byType(UiKitView), findsOneWidget);
    } finally {
      debugDefaultTargetPlatformOverride = null;
      TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
          .setMockMethodCallHandler(SystemChannels.platform_views, null);
    }
  });
}

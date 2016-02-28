/*
 *  Copyright (C) 2015 GuDong <gudong.name@gmail.com>
 *
 *  This file is part of GdTranslate
 *
 *  GdTranslate is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  GdTranslate is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with GdTranslate.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package name.gudong.translate.reject.components;

import dagger.Component;
import name.gudong.translate.listener.ListenClipboardService;
import name.gudong.translate.reject.ActivityScope;
import name.gudong.translate.reject.modules.ServiceModule;

/**
 * Created by GuDong on 2/28/16 19:12.
 * Contact with gudong.name@gmail.com.
 */
@ActivityScope
@Component(modules = {ServiceModule.class},dependencies = {AppComponent.class})
public interface ServiceComponent {
    void inject(ListenClipboardService service);
}

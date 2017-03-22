#!/bin/sh

# Intygsprojektets testare vill ha git commit hooks som delas med varandra.
# Denna enkla lösning tar bort ursprunglig hooks-director i git-repot och
# ersätter med en folder innehallande script som testarna sjalva tagit fram.
#
# Notera att Om aven utvecklarna borjar anvanda git-hooks  pa klientsidan
# som aven vi ska anvandda sa behover den har losningen andras da utvecklarnas
# git-hooks skulle skrivas over av oss.

GIT_HOOKS_DIR=`git rev-parse --git-dir`/hooks
GIT_ROOT_DIR=`git rev-parse --show-toplevel`
ACCEPTANCE_HOOK_DIR=$GIT_ROOT_DIR/test/acceptance/git_hooks

# Ta bort det redan existerande hooks-foldern om den existerar.
if [ -e $GIT_HOOKS_DIR ]; then
  # Om det ar en mjuklank sa ar den var och behover inte tas bort.  
  if [ ! -h $GIT_HOOKS_DIR ]; then
    echo Raderar projektets hooks-folder: $GIT_HOOKS_DIR
    rm -r $GIT_HOOKS_DIR

    # Lanka in tests script till samma foldernamn istallet
    echo Mjuklankar testarnas git-hooks folder till projektets: ln -s $ACCEPTANCE_HOOK_DIR $GIT_HOOKS_DIR
    ln -s $ACCEPTANCE_HOOK_DIR $GIT_HOOKS_DIR
  fi
fi


